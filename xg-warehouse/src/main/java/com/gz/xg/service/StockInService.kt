package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.dto.StockInExecutionResult
import com.gz.xg.domain.dto.StockInFailTag
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.StockInMapStruct
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.service.plus.StockInPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.util.BarcodeLockUtil
import com.gz.xg.util.DateUtil
import com.gz.xg.util.IdUtil
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal

/**
 * 入库服务，负责入库单生成、标签关联保存以及库存落库。
 */
@Service
class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService,
    private val stockInMapStruct: StockInMapStruct,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
    private val prepRecordService: PrepRecordService,
    private val u8StockInSyncService: U8StockInSyncService
) : BaseService() {

    /**
     * 新增入库单，按 (业务类型, ERP单号) 分批提交入库。返回执行结果（成功数与未完成条码列表）。
     */
    fun add(req: AddStockIn): StockInExecutionResult {
        return addByType(req, "直接入库")
    }

    /**
     * 新增退货入库单，按 (业务类型, ERP单号) 分批提交入库。返回执行结果（成功数与未完成条码列表）。
     */
    fun addReturn(req: AddStockIn): StockInExecutionResult {
        return addByType(req, "退货入库")
    }

    /**
     * 分页查询入库单，并回填关联标签详情。
     */
    fun page(search: StockSearch, current: Long, size: Long): Map<String, Any> {
        val page = Page<StockIn>(current, size)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }
        val wrapper = MPJLambdaWrapper<StockIn>()
            .like(!search.no.isNullOrBlank(), StockIn::getReceiptNo, search.no)
            .like(!search.locCode.isNullOrBlank(), StockIn::getLoc, search.locCode)
            .eq(!search.warehouseCode.isNullOrBlank(), StockIn::getWarehouseCode, search.warehouseCode)
            .between(StockIn::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockIn::getId)

        val pageObj = plusService.page(page, wrapper)

        val dtoList = stockInMapStruct.toDtoList(pageObj.records)

        val ids = dtoList.map { it.id }
        if (ids.isNotEmpty()){
            val allStockInTags = stockInTagPlusService.listByPIds(ids)
            val tagNosByPId = allStockInTags.groupBy({ it.pId }, { it.tagNo })
            val allTagNos = allStockInTags.map { it.tagNo }
            val prodTagMap = prodTagPlusService.listByTagNos(allTagNos).associateBy { it.tagNo }

            dtoList.forEach { dtoIt ->
                val tagNos = tagNosByPId[dtoIt.id] ?: emptyList()
                dtoIt.tags = tagNos.mapNotNull { prodTagMap[it] }
            }
        }

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }

    private fun addByType(req: AddStockIn, type: String): StockInExecutionResult {
        return BarcodeLockUtil.withLock(
            barcodes = req.tagNos,
            onConflict = { conflictTag ->
                throw WebException("条码[$conflictTag]正在入库处理中，请勿重复提交")
            }
        ) {
            doAddByType(req, type)
        }
    }

    private fun doAddByType(req: AddStockIn, type: String): StockInExecutionResult {
        val locArchive = locArchivePlusService.getById(req.locId)
            ?: throw WebException("该库位不存在")
        val warehouseCode = req.warehouseCode

        val resolved = billTagResolver.resolve(req.tagNos)

        // STK-1：防重复入库查库存表（StockInventory.deleted=0）
        val occupied = stockInventoryPlusService.listByTagNos(resolved.tagNos)
        if (occupied.isNotEmpty()) {
            throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已在库存中，不能重复入库")
        }

        val prodTagMap = resolved.prodTags.associateBy { it.tagNo }
        val failTags = mutableListOf<StockInFailTag>()

        // 提前批量查询所有关联的 ProdOrder
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        val orderMap = if (prodOrderIds.isNotEmpty()) {
            productionOrderPlusService.listByIds(prodOrderIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        // 缓存销售单反查到的生产订单：key: "$invCode:$salesOrderNo" -> ProdOrder?
        val momOrderCache = mutableMapOf<String, ProdOrder?>()

        // 为每个标签计算目标 ERP 类型与订单号 (targetType: 1-采购, 2-生产, targetErpOrderId: String, erpOrderNo: String)
        data class TagOrderTarget(
            val tagNo: String,
            val prodTag: VProdTag,
            val targetType: Int,
            val targetErpOrderId: String,
            val momOrder: ProdOrder? = null,
            val erpOrderNo: String = ""
        )

        val validTargets = mutableListOf<TagOrderTarget>()

        for (tagNo in resolved.tagNos) {
            val prodTag = prodTagMap[tagNo]
            if (prodTag == null) {
                failTags.add(StockInFailTag(tagNo, "未找到该标签记录"))
                continue
            }
            val prodOrder = prodTag.prodOrderId?.let { orderMap[it] }
            if (prodOrder == null) {
                failTags.add(StockInFailTag(tagNo, "未找到该标签对应的订单记录"))
                continue
            }
            val orderType = prodOrder.type
            val erpOrderId = prodOrder.erpOrderId
            val orderNo = prodOrder.erpOrderNo ?: prodOrder.prodNo ?: ""

            if (orderType == 1) {
                // 采购订单：必须有 erpOrderId
                if (erpOrderId.isNullOrBlank()) {
                    failTags.add(StockInFailTag(tagNo, "采购订单[${orderNo}]未关联有效的U8采购订单号"))
                } else {
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 1, erpOrderId, null, orderNo))
                }
            } else if (orderType == 2) {
                // 生产订单：必须有 erpOrderId
                if (erpOrderId.isNullOrBlank()) {
                    failTags.add(StockInFailTag(tagNo, "生产订单[${orderNo}]未关联有效的U8生产订单号"))
                } else {
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 2, erpOrderId, null, orderNo))
                }
            } else if (orderType == 0) {
                // 销售订单：通过 inventoryCode + 生产单 m3（关联销售单号）反查生产订单 (type == 2)
                val invCode = prodOrder.inventoryCode ?: prodTag.inventoryCode ?: ""
                val salesOrderNo = prodOrder.erpOrderNo
                val cacheKey = "$invCode:${salesOrderNo ?: ""}"

                val momOrder = momOrderCache.getOrPut(cacheKey) {
                    productionOrderPlusService.findLatestMomOrderByInvCodeAndSalesOrderNo(invCode, salesOrderNo)
                }

                if (momOrder == null || momOrder.erpOrderId.isNullOrBlank()) {
                    val detailReason = when {
                        invCode.isBlank() -> "销售单缺少存货编码"
                        salesOrderNo.isNullOrBlank() -> "销售单缺少销售单号"
                        else -> "存货[${invCode}]、销售单[${salesOrderNo}]未找到关联的有效生产订单(已下发U8)"
                    }
                    failTags.add(StockInFailTag(tagNo, detailReason))
                } else {
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 2, momOrder.erpOrderId, momOrder, orderNo))
                }
            } else {
                failTags.add(StockInFailTag(tagNo, "未知订单类型[${orderType}]，无法入库"))
            }
        }

        val effectiveWarehouseCode = warehouseCode?.takeIf { it.isNotBlank() } ?: "01"

        // 销售单直接入库到 01 仓时，校验入库数量（本次 + 历史在库）不大于销售单的 qty
        if (type == "直接入库" && effectiveWarehouseCode == "01") {
            val salesTargets = validTargets.filter {
                it.prodTag.prodOrderId?.let { id -> orderMap[id]?.type } == 0
            }
            if (salesTargets.isNotEmpty()) {
                val targetsByOrderId = salesTargets.groupBy { it.prodTag.prodOrderId!! }
                for ((orderId, items) in targetsByOrderId) {
                    val prodOrder = orderMap[orderId] ?: continue
                    val planQty = prodOrder.qty ?: BigDecimal.ZERO
                    val curQty = items.fold(BigDecimal.ZERO) { acc, item ->
                        acc.add(item.prodTag.qty ?: BigDecimal.ZERO)
                    }
                    val historyQty = stockInventoryPlusService.sumQtyByProdOrderIdAndWarehouse(orderId, effectiveWarehouseCode)
                    val totalQty = historyQty.add(curQty)
                    if (totalQty > planQty) {
                        val remainQty = planQty.subtract(historyQty).max(BigDecimal.ZERO)
                        val salesOrderNo = prodOrder.erpOrderNo ?: prodOrder.prodNo ?: ""
                        val invCode = prodOrder.inventoryCode ?: ""
                        throw WebException("销售单【$salesOrderNo】存货【$invCode】计划数量为 $planQty，01仓历史已入库 $historyQty，本次尝试入库 $curQty，剩余最多可入 $remainQty，超出计划数量")
                    }
                }
            }
        }

        // 按 (业务分类, 目标ERP单号, 批次订单号) 分组
        val groupedTargets = validTargets.groupBy { Triple(it.targetType, it.targetErpOrderId, it.erpOrderNo) }

        var successCount = 0

        // 逐批次独立循环执行：每组对应一次独立的 U8 推送和 WMS 入库
        for ((groupKey, groupItems) in groupedTargets) {
            val (targetType, targetErpOrderId, _) = groupKey
            val groupTagNos = groupItems.map { it.tagNo }
            val groupProdTags = groupItems.map { it.prodTag }

            // 计算该组的统计汇总
            val sumQty = groupProdTags.fold(BigDecimal.ZERO) { acc, tag ->
                acc.add(tag.qty ?: BigDecimal.ZERO)
            }
            val sumGross = groupProdTags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.grossWeight ?: BigDecimal.ZERO) }
            val sumNet = groupProdTags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.netWeight ?: BigDecimal.ZERO) }
            val groupTotal = ProdTagTotal(sumQty, sumGross, sumNet)
            val groupResolved = ResolvedTags(groupTagNos, groupProdTags, groupTotal)

            // 构造专属于该批次的销售单反查生产单映射
            val groupMomOrderMap = groupItems.filter { it.momOrder != null }.associate { it.tagNo to it.momOrder!! }

            val receiptNo = sysSequenceService.generateStockIn()

            val u8BatchByTagNo = groupItems.associate { item ->
                val order = item.prodTag.prodOrderId?.let { orderMap[it] }
                item.tagNo to u8BatchOf(order)
            }

            try {
                TransactionTemplate(transactionManager).execute {
                    saveSingleBatchStockIn(
                        resolved = groupResolved,
                        type = type,
                        receiptNo = receiptNo,
                        locArchive = locArchive,
                        targetType = targetType,
                        targetErpOrderId = targetErpOrderId,
                        momOrderMap = groupMomOrderMap,
                        u8BatchByTagNo = u8BatchByTagNo,
                        warehouseCode = warehouseCode
                    )
                }
                successCount += groupTagNos.size
                log.info("批次入库成功: receiptNo={}, targetType={}, erpOrderId={}, 条码数={}",
                    receiptNo, targetType, targetErpOrderId, groupTagNos.size)
            } catch (e: Exception) {
                log.error("批次入库失败: erpOrderId={}, 错误: {}", targetErpOrderId, e.message)
                val rawMsg = e.message ?: ""
                val errMsg = when {
                    e is DuplicateKeyException || rawMsg.contains("IX_StockInventory_tagNo") || rawMsg.contains("UNIQUE KEY") ->
                        "条码已在库存中或并发冲突，请勿重复提交"
                    e is DataIntegrityViolationException ->
                        "数据冲突或已存在相同记录，请重试"
                    !e.message.isNullOrBlank() -> e.message!!
                    else -> "入库异常"
                }
                for (tagNo in groupTagNos) {
                    failTags.add(StockInFailTag(tagNo, errMsg))
                }
            }
        }

        return StockInExecutionResult(successCount, failTags)
    }

    private fun saveSingleBatchStockIn(
        resolved: ResolvedTags,
        type: String,
        receiptNo: String,
        locArchive: LocArchive,
        targetType: Int,
        targetErpOrderId: String,
        momOrderMap: Map<String, ProdOrder>,
        u8BatchByTagNo: Map<String, String>,
        warehouseCode: String? = null
    ) {
        val id = IdUtil.generateId()
        val (userId, username, realName) = UserContext.require()

        val stockIn = StockIn().apply {
            this.id = id
            this.receiptNo = receiptNo
            qty = resolved.total.qty
            grossWeight = resolved.total.grossWeight
            netWeight = resolved.total.netWeight
            this.type = type
            loc = locArchive.locCode
            this.userId = userId
            this.username = username
            this.realName = realName
            this.warehouseCode = warehouseCode
        }

        // 调用 U8 同步，获取 U8 同步结果
        val syncResult = u8StockInSyncService.syncSingleBatch(
            resolved = resolved,
            stockIn = stockIn,
            locArchive = locArchive,
            u8BatchByTagNo = u8BatchByTagNo,
            targetType = targetType,
            targetErpOrderId = targetErpOrderId,
            momOrderMap = momOrderMap
        )

        if (!syncResult.errorMessage.isNullOrBlank()) {
            throw WebException(syncResult.errorMessage)
        }

        val tags = resolved.tagNos.map { tagNo ->
            val syncStatus = syncResult.statusMap[tagNo] ?: 1

            StockInTag().apply {
                pId = id
                this.tagNo = tagNo
                locId = locArchive.id
                locCode = locArchive.locCode
                u8Sync = syncStatus
            }
        }

        plusService.save(stockIn)
        stockInTagPlusService.saveBatch(tags)
        stockInventoryService.addBatch(resolved.prodTags, locArchive, u8BatchByTagNo, warehouseCode)
    }

    fun addUnpack(tagNos: List<String>, locId: String, receiptNo: String) {
        val locArchive = locArchivePlusService.getById(locId)
            ?: throw WebException("该库位不存在")

        val alreadyIn = stockInventoryPlusService.listByTagNos(tagNos).map { it.tagNo }.toSet()
        val pending = tagNos.filter { it !in alreadyIn }
        if (pending.isEmpty()) return

        val existingStockIn = plusService.findByReceiptNo(receiptNo)
        if (existingStockIn != null) {
            val resolved = billTagResolver.resolve(pending)
            val u8BatchByTagNo = buildUnpackBatchNos(resolved)
            stockInventoryService.addBatch(resolved.prodTags, locArchive, u8BatchByTagNo, existingStockIn.warehouseCode)
            return
        }

        val resolved = billTagResolver.resolve(pending)
        val firstTag = resolved.prodTags.first()
        val prodOrder = firstTag.prodOrderId?.let { productionOrderPlusService.getById(it) }
            ?: throw WebException("未找到该标签对应的订单记录")

        val orderNo = prodOrder.erpOrderNo ?: prodOrder.prodNo ?: ""
        var targetType = prodOrder.type ?: throw WebException("未知订单类型，无法入库")
        var targetErpOrderId = prodOrder.erpOrderId ?: ""
        val momOrderMap = mutableMapOf<String, ProdOrder>()

        when (targetType) {
            1, 2 -> {
                if (targetErpOrderId.isBlank()) {
                    throw WebException("订单[${orderNo}]未关联有效的U8订单号")
                }
            }
            0 -> {
                val invCode = prodOrder.inventoryCode ?: firstTag.inventoryCode ?: ""
                val salesOrderNo = prodOrder.erpOrderNo
                val momOrder = productionOrderPlusService.findLatestMomOrderByInvCodeAndSalesOrderNo(
                    invCode, salesOrderNo
                ) ?: throw WebException("存货[${invCode}]、销售单[${salesOrderNo ?: ""}]未找到关联的有效生产订单(已下发U8)")
                targetType = 2
                targetErpOrderId = momOrder.erpOrderId
                resolved.prodTags.forEach { momOrderMap[it.tagNo] = momOrder }
            }
            else -> throw WebException("未知订单类型[${targetType}]，无法入库")
        }

        val u8BatchByTagNo = buildUnpackBatchNos(resolved)

        TransactionTemplate(transactionManager).execute {
            saveSingleBatchStockIn(
                resolved = resolved,
                type = "拆箱入库",
                receiptNo = receiptNo,
                locArchive = locArchive,
                targetType = targetType,
                targetErpOrderId = targetErpOrderId,
                momOrderMap = momOrderMap,
                u8BatchByTagNo = u8BatchByTagNo
            )
        }
    }

    private fun buildUnpackBatchNos(resolved: ResolvedTags): Map<String, String> {
        val orderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        val orderMap = if (orderIds.isEmpty()) {
            emptyMap()
        } else {
            productionOrderPlusService.listByIds(orderIds).associateBy { it.id }
        }
        return resolved.prodTags.associate { tag ->
            tag.tagNo to u8BatchOf(tag.prodOrderId?.let { orderMap[it] })
        }
    }

    /**
     * 采购单、销售单用 erpOrderNo；生产单用 m3（关联销售单号）。
     */
    private fun u8BatchOf(prodOrder: ProdOrder?): String {
        if (prodOrder == null) return ""
        return when (prodOrder.type) {
            2 -> prodOrder.m3 ?: ""
            else -> prodOrder.erpOrderNo ?: ""
        }
    }

}
