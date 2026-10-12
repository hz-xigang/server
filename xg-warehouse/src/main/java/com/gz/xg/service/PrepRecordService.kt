package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.domain.mapstruct.PrepRecordMapStruct
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import com.gz.xg.service.plus.PrepOrderPlusService
import com.gz.xg.service.plus.PrepRecordPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal

@Service
class PrepRecordService(
    private val plusService: PrepRecordPlusService,
    private val prepOrderPlusService: PrepOrderPlusService,
    private val prepOrderDetailPlusService: PrepOrderDetailPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val pmt: PlatformTransactionManager,
    private val stockMoveService: StockMoveService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prepRecordMapStruct: PrepRecordMapStruct
) : BaseService() {

    fun add(addStockOrder: AddStockOrder): String {
        val tagNos = addStockOrder.tagNos
        if (tagNos.isEmpty()) {
            throw WebException("备料标签列表不能为空")
        }
        if (addStockOrder.locCode.isBlank()) {
            throw WebException("目标库位不能为空")
        }

        val prepOrder = prepOrderPlusService.findByNo(addStockOrder.no)
        val prodTags = prodTagPlusService.listByTagNos(tagNos)
        if (prodTags.size != tagNos.distinct().size) {
            val foundTagNos = prodTags.map { it.tagNo }.toSet()
            val missing = tagNos.distinct() - foundTagNos
            throw WebException("标签【${missing.joinToString(",")}】不存在")
        }

        val orderDetails = prepOrderDetailPlusService.listByPId(prepOrder.id)
        if (orderDetails.isEmpty()) {
            throw WebException("【${prepOrder.prepNo}】备料指令单明细为空")
        }

        // 校验本次扫描的所有标签存货是否都在指令单明细中
        val orderInvCodes = orderDetails.map { it.inventoryCode }.toSet()
        prodTags.forEach {
            if (!orderInvCodes.contains(it.inventoryCode)) {
                throw WebException("【${it.tagNo}】存货编码【${it.inventoryCode}】不在备料指令清单中")
            }
        }

        // 1. 获取该指令单历史已备记录，并校验本次条码是否重复扫描
        val historyRecords = plusService.listByPrepOrderNo(prepOrder.prepNo)
        val historyTagNos = historyRecords.mapNotNull { it.tagNo }.filter { it.isNotBlank() }.toSet()
        val duplicateTags = tagNos.filter { it in historyTagNos }
        if (duplicateTags.isNotEmpty()) {
            throw WebException("标签【${duplicateTags.joinToString(",")}】已在此备料单中备料，请勿重复扫描")
        }

        // 2. 根据 prodTag 统计历史已备数量（按存货编码）
        val historyProdTags = if (historyTagNos.isNotEmpty()) {
            prodTagPlusService.listByTagNos(historyTagNos.toList())
        } else {
            emptyList()
        }
        val historyQtyByInv = historyProdTags.groupBy { it.inventoryCode }.mapValues { (_, tags) ->
            tags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.qty ?: BigDecimal.ZERO) }
        }

        // 3. 统计本次扫描数量（按存货编码）
        val currentQtyByInv = prodTags.groupBy { it.inventoryCode }.mapValues { (_, tags) ->
            tags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.qty ?: BigDecimal.ZERO) }
        }

        // 4. 计算指令单各存货的计划总数量
        val planQtyByInv = orderDetails.groupBy { it.inventoryCode }.mapValues { (_, details) ->
            details.fold(BigDecimal.ZERO) { acc, d ->
                val planQty = d.totalQty?.takeIf { it > BigDecimal.ZERO } ?: d.qty ?: BigDecimal.ZERO
                acc.add(planQty)
            }
        }

        // 5. 校验本次 + 历史是否超过指令单计划数量
        currentQtyByInv.forEach { (invCode, curQty) ->
            val planQty = planQtyByInv[invCode] ?: BigDecimal.ZERO
            val histQty = historyQtyByInv[invCode] ?: BigDecimal.ZERO
            val totalQty = histQty.add(curQty)
            if (totalQty > planQty) {
                val remainQty = planQty.subtract(histQty).max(BigDecimal.ZERO)
                throw WebException("存货编码【$invCode】计划数量为 $planQty，已备数量为 $histQty，本次尝试备 $curQty，剩余最多可备 $remainQty，超出计划数量")
            }
        }

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            val packLoc = locArchivePlusService.byCode(addStockOrder.locCode)
            val user = UserContext.get()
            val records = arrayListOf<PrepRecord>()

            prodTags.forEach { it ->
                val detail = orderDetails.find { oIt -> oIt.inventoryCode == it.inventoryCode }
                    ?: throw WebException("【${it.tagNo}】存货编码【${it.inventoryCode}】不在备料指令清单中")

                val record = prepRecordMapStruct.detailToEntity(detail)
                record.id = IdUtil.generateId()
                record.prepOrderNo = prepOrder.prepNo
                record.tagNo = it.tagNo
                record.qty = it.qty
                if (user != null) {
                    record.userId = user.userId
                    record.username = user.username
                    record.realName = user.realName
                }
                records.add(record)
            }

            plusService.saveBatch(records)

            // 6. 判断指令单所有明细存货是否全部备料完成
            val isAllCompleted = planQtyByInv.all { (invCode, planQty) ->
                val histQty = historyQtyByInv[invCode] ?: BigDecimal.ZERO
                val curQty = currentQtyByInv[invCode] ?: BigDecimal.ZERO
                val totalQty = histQty.add(curQty)
                totalQty >= planQty
            }

            // 全部完成标记为 2(已备料)，未完成标记为 1(备料中，供下次继续)
            prepOrder.status = if (isAllCompleted) 2 else 1
            prepOrderPlusService.updateById(prepOrder)

            val u8ErrorMsg = stockMoveService.addByPrep(records, packLoc)
            pmt.commit(status)
            log.info(
                "备料确认成功: prepNo={}, 本次备料条数={}, 指令单状态={}",
                prepOrder.prepNo, records.size, if (isAllCompleted) "已完成(2)" else "未完成(1)"
            )
            return u8ErrorMsg
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message ?: "备料失败", e)
        }
    }

}
