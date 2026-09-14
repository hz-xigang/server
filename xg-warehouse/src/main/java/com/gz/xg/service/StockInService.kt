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

        // 缓存销售单反查到的生产订单：key: "$invCode:${customerCode ?: ""}" -> ProdOrder?
        val momOrderCache = mutableMapOf<String, ProdOrder?>()

        // 为每个标签计算目标 ERP 类型与订单号 (targetType: 1-采购, 2-生产, targetErpOrderId: String)
        data class TagOrderTarget(
            val tagNo: String,
            val prodTag: VProdTag,
            val targetType: Int,
            val targetErpOrderId: String,
            val momOrder: ProdOrder? = null
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
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 1, erpOrderId))
                }
            } else if (orderType == 2) {
                // 生产订单：必须有 erpOrderId
                if (erpOrderId.isNullOrBlank()) {
                    failTags.add(StockInFailTag(tagNo, "生产订单[${orderNo}]未关联有效的U8生产订单号"))
                } else {
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 2, erpOrderId))
                }
            } else if (orderType == 0) {
                // 销售订单：通过 inventoryCode + customerCode 反查生产订单 (type == 2)
                val invCode = prodOrder.inventoryCode ?: prodTag.inventoryCode ?: ""
                val custCode = prodOrder.customerCode ?: prodTag.customerCode
                val cacheKey = "$invCode:${custCode ?: ""}"

                val momOrder = momOrderCache.getOrPut(cacheKey) {
                    productionOrderPlusService.findLatestMomOrderByInvCodeAndCustomerCode(invCode, custCode)
                }

                if (momOrder == null || momOrder.erpOrderId.isNullOrBlank()) {
                    val detailReason = if (invCode.isBlank()) {
                        "销售单缺少存货编码"
                    } else {
                        "存货[${invCode}]${if (!custCode.isNullOrBlank()) "、客户[${custCode}]" else ""}未找到关联的有效生产订单(已下发U8)"
                    }
                    failTags.add(StockInFailTag(tagNo, detailReason))
                } else {
                    validTargets.add(TagOrderTarget(tagNo, prodTag, 2, momOrder.erpOrderId, momOrder))
                }
            } else {
                failTags.add(StockInFailTag(tagNo, "未知订单类型[${orderType}]，无法入库"))
            }
        }

        // 按 (业务分类, 目标ERP单号) 分组
        val groupedTargets = validTargets.groupBy { Pair(it.targetType, it.targetErpOrderId) }

        var successCount = 0

        // 逐批次独立循环执行：每组对应一次独立的 U8 推送和 WMS 入库
        for ((groupKey, groupItems) in groupedTargets) {
            val (targetType, targetErpOrderId) = groupKey
            val groupTagNos = groupItems.map { it.tagNo }
            val groupProdTags = groupItems.map { it.prodTag }

            // 计算该组的统计汇总
            val sumQty = groupProdTags.sumOf { it.qty ?: 0 }
            val sumGross = groupProdTags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.grossWeight ?: BigDecimal.ZERO) }
            val sumNet = groupProdTags.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.netWeight ?: BigDecimal.ZERO) }
            val groupTotal = ProdTagTotal(sumQty, sumGross, sumNet)
            val groupResolved = ResolvedTags(groupTagNos, groupProdTags, groupTotal)

            // 构造专属于该批次的销售单反查生产单映射
            val groupMomOrderMap = groupItems.filter { it.momOrder != null }.associate { it.tagNo to it.momOrder!! }

            // 为该批次生成独立的入库单号
            val receiptNo = sysSequenceService.generateStockIn()

            try {
                // 单批次内部采用独立事务，保证 WMS 落库与 U8 推送的局部原子性
                TransactionTemplate(transactionManager).execute {
                    saveSingleBatchStockIn(
                        resolved = groupResolved,
                        type = type,
                        receiptNo = receiptNo,
                        locArchive = locArchive,
                        targetType = targetType,
                        targetErpOrderId = targetErpOrderId,
                        momOrderMap = groupMomOrderMap
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

    /**
     * 单个 ERP 订单批次的入库处理：先推 U8，成功后写 WMS 主单、标签流水、库存。
     */
    private fun saveSingleBatchStockIn(
        resolved: ResolvedTags,
        type: String,
        receiptNo: String,
        locArchive: LocArchive,
        targetType: Int,
        targetErpOrderId: String,
        momOrderMap: Map<String, ProdOrder>
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
        }

        val u8Batch = IdUtil.generateId()

        // 调用 U8 同步，获取 U8 同步结果
        val syncResult = u8StockInSyncService.syncSingleBatch(
            resolved = resolved,
            stockIn = stockIn,
            locArchive = locArchive,
            u8Batch = u8Batch,
            targetType = targetType,
            targetErpOrderId = targetErpOrderId,
            momOrderMap = momOrderMap
        )

        if (!syncResult.errorMessage.isNullOrBlank()) {
            throw WebException(syncResult.errorMessage)
        }

        val prodTagMap = resolved.prodTags.associateBy { it.tagNo }

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
        stockInventoryService.addBatch(resolved.prodTags, locArchive, u8Batch)
    }

}
