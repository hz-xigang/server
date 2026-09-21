package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.UnpackTagVo
import com.gz.xg.domain.entity.PrintLog
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.req.UnpackTagReq
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PrintLogMapper
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.util.BarcodeLockUtil
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class UnpackService(
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val palletService: PalletService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockInService: StockInService,
    private val stockOutService: StockOutService,
    private val sysSequenceService: SysSequenceService,
    private val printLogMapper: PrintLogMapper,
    private val transactionManager: PlatformTransactionManager
) : BaseService() {

    fun unpack(req: UnpackTagReq): List<UnpackTagVo> {
        val tagNo = req.tagNo.trim()
        val qtys = req.qtys
        if (qtys.size < 2) throw WebException("拆箱至少拆成2个条码")
        if (qtys.any { it <= 0 }) throw WebException("拆分数量必须大于0")

        return BarcodeLockUtil.withLock(
            barcodes = listOf(tagNo),
            onConflict = { throw WebException("条码[$it]正在处理中，请勿重复提交") }
        ) {
            doUnpack(tagNo, qtys)
        }
    }

    private fun doUnpack(tagNo: String, qtys: List<Int>): List<UnpackTagVo> {
        var children = prodTagPlusService.listByParentTagNo(tagNo)
        val origin = prodTagPlusService.findEntityByTagNo(tagNo)
        val originInv = stockInventoryPlusService.findByTagNo(tagNo)
        val everOut = stockInventoryPlusService.listByTagNosWithDeleted(listOf(tagNo))
            .any { it.deleted != null && it.deleted != 0 }

        if (children.isEmpty()) {
            if (origin == null) throw WebException("【$tagNo】纸箱标签不存在")
            if (origin.qty == null || origin.qty <= 0) throw WebException("【$tagNo】原条码数量无效")
            if (qtys.sum() != origin.qty) {
                throw WebException("拆分数量合计[${qtys.sum()}]必须等于原条码数量[${origin.qty}]")
            }
            if (originInv == null && everOut) {
                throw WebException("【$tagNo】已出库，不能拆箱")
            }

            val tagNos = sysSequenceService.generateCartons(qtys.size)
            val outNo = if (originInv != null) sysSequenceService.generateStockOut() else ""
            val inNo = if (originInv != null) sysSequenceService.generateStockIn() else ""
            val weights = splitWeights(
                origin.netWeight ?: BigDecimal.ZERO,
                origin.grossWeight ?: BigDecimal.ZERO,
                origin.qty,
                qtys
            )

            TransactionTemplate(transactionManager).execute {
                val userInfo = UserContext.require()
                val now = LocalDateTime.now()
                val newTags = qtys.mapIndexed { index, qty ->
                    ProdTag().apply {
                        id = IdUtil.generateId()
                        this.tagNo = tagNos[index]
                        prodOrderId = origin.prodOrderId
                        this.qty = qty
                        netWeight = weights[index].first
                        grossWeight = weights[index].second
                        userId = userInfo.userId
                        username = userInfo.username
                        realName = userInfo.realName
                        createTime = now
                        deleted = 0
                        m1 = tagNo
                        m2 = outNo
                        m3 = inNo
                    }
                }
                prodTagPlusService.saveBatch(newTags)
                newTags.forEach { tag ->
                    printLogMapper.insert(PrintLog().apply {
                        id = IdUtil.generateId()
                        no = tag.tagNo
                        type = 1
                        userId = userInfo.userId
                        username = userInfo.username
                        realName = userInfo.realName
                    })
                }
                palletService.replaceTag(tagNo, tagNos)
                if (originInv == null) {
                    voidOrigin(origin.id)
                }
            }
            children = prodTagPlusService.listByParentTagNo(tagNo)
        } else {
            val childQtys = children.map { it.qty ?: 0 }.sorted()
            if (childQtys != qtys.sorted()) {
                throw WebException("【$tagNo】已拆箱，拆分数量与原记录不一致")
            }
        }

        if (children.isEmpty()) throw WebException("拆箱失败：未生成新条码")

        val latestInv = stockInventoryPlusService.findByTagNo(tagNo)
        if (latestInv != null) {
            val locId = latestInv.locId ?: throw WebException("【$tagNo】库存库位信息缺失，无法拆箱入库")
            val outNo = children.first().m2?.takeIf { it.isNotBlank() }
                ?: throw WebException("拆箱出库单号缺失")
            val inNo = children.first().m3?.takeIf { it.isNotBlank() }
                ?: throw WebException("拆箱入库单号缺失")
            stockOutService.addByUnpack(tagNo, outNo)
            stockInService.addUnpack(children.map { it.tagNo }, locId, inNo)
            origin?.id?.let { voidOrigin(it) }
        } else {
            val inNo = children.first().m3
            val pendingIn = children.filter { stockInventoryPlusService.findByTagNo(it.tagNo) == null }
            if (pendingIn.isNotEmpty() && !inNo.isNullOrBlank()) {
                val locId = stockInventoryPlusService.listByTagNosWithDeleted(listOf(tagNo))
                    .firstOrNull()?.locId
                    ?: throw WebException("【$tagNo】找不到原库位，无法拆箱入库")
                stockInService.addUnpack(children.map { it.tagNo }, locId, inNo)
            }
            origin?.id?.let { voidOrigin(it) }
        }

        log.info("拆箱完成: origin={}, newTags={}", tagNo, children.joinToString(",") { it.tagNo })
        return toVos(children)
    }

    private fun toVos(children: List<ProdTag>): List<UnpackTagVo> {
        val viewMap = prodTagPlusService.listByTagNos(children.map { it.tagNo }).associateBy { it.tagNo }
        val orderIds = children.mapNotNull { it.prodOrderId }.distinct()
        val orderMap = if (orderIds.isEmpty()) emptyMap() else productionOrderPlusService.listByIds(orderIds).associateBy { it.id }
        return children.map { tag ->
            val view = viewMap[tag.tagNo]
            val order = tag.prodOrderId?.let { orderMap[it] }
            UnpackTagVo(
                tagNo = tag.tagNo,
                customerCode = view?.customerCode ?: order?.customerCode,
                erpOrderNo = order?.erpOrderNo,
                prodNo = view?.prodNo ?: order?.prodNo,
                productCategory = view?.productCategory ?: order?.productCategory,
                spec = view?.spec ?: order?.spec,
                inventoryCode = view?.inventoryCode ?: order?.inventoryCode,
                inventoryName = view?.inventoryName ?: order?.inventoryName,
                qty = tag.qty,
                netWeight = tag.netWeight,
                grossWeight = tag.grossWeight
            )
        }
    }

    private fun voidOrigin(originId: String) {
        TransactionTemplate(transactionManager).execute {
            LambdaUpdateChainWrapper(prodTagPlusService.baseMapper)
                .set(ProdTag::getDeleted, 1)
                .eq(ProdTag::getId, originId)
                .eq(ProdTag::getDeleted, 0)
                .update()
        }
    }

    private fun splitWeights(
        originNet: BigDecimal,
        originGross: BigDecimal,
        originQty: Int,
        qtys: List<Int>
    ): List<Pair<BigDecimal, BigDecimal>> {
        val totalQty = BigDecimal(originQty)
        val nets = mutableListOf<BigDecimal>()
        val grosses = mutableListOf<BigDecimal>()
        var netUsed = BigDecimal.ZERO
        var grossUsed = BigDecimal.ZERO
        qtys.forEachIndexed { index, qty ->
            if (index == qtys.lastIndex) {
                nets.add(originNet.subtract(netUsed))
                grosses.add(originGross.subtract(grossUsed))
            } else {
                val ratio = BigDecimal(qty).divide(totalQty, 10, RoundingMode.HALF_UP)
                val net = originNet.multiply(ratio).setScale(2, RoundingMode.HALF_UP)
                val gross = originGross.multiply(ratio).setScale(2, RoundingMode.HALF_UP)
                nets.add(net)
                grosses.add(gross)
                netUsed = netUsed.add(net)
                grossUsed = grossUsed.add(gross)
            }
        }
        return nets.zip(grosses)
    }
}
