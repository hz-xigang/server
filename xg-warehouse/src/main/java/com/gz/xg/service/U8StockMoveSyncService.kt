package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.u8.dto.U8StockMoveDetailRequest
import com.gz.xg.u8.dto.U8StockMoveRequest
import com.gz.xg.u8.service.U8StockMoveService
import com.gz.xg.util.LocUtil
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 移库业务同步 U8 货位调整单服务。
 */
@Service
class U8StockMoveSyncService(
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val u8StockMoveService: U8StockMoveService
) : BaseService() {

    /**
     * 同步移库数据至 U8 货位调整单。
     * 明细按 (存货编码, 批次号) 汇总。货位按库位 id 升序拼接。
     * 例如批次当前在开料01、部分移到货架01，且开料01 的库位 id 更小时，转入为开料01,货架01。
     * 仅对 ProdOrder.type == 2 (生产订单) 或 ProdOrder.type == 0 (销售订单) 的标签发起推送。
     *
     * @param resolved 解析后的标签结果
     * @param originLocMap 标签原库位映射 (tagNo -> originStockInventory)
     * @param stockMove 移库主单
     * @param targetLocArchive 目标库位
     * @return 每个 tagNo 对应的 u8Sync 状态（0-未同步, 1-已同步, 2-不需同步）
     */
    fun syncStockMove(
        resolved: ResolvedTags,
        originLocMap: Map<String, StockInventory>,
        stockMove: StockMove,
        targetLocArchive: LocArchive
    ): U8SyncResult {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return U8SyncResult(statusMap = resolved.prodTags.associate { it.tagNo to 2 })
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }

        val tagMap = resolved.prodTags.associateBy { it.tagNo }
        val syncStatusByTagNo = mutableMapOf<String, Int>()
        var errorMsg: String? = null

        // 筛选出 type == 2 或 type == 0 的标签项
        val eligibleTags = mutableListOf<VProdTag>()
        resolved.tagNos.forEach { tagNo ->
            val prodTag = tagMap[tagNo]
            val order = prodTag?.prodOrderId?.let { orderMap[it] }
            if (order != null && (order.type == 2 || order.type == 0)) {
                eligibleTags.add(prodTag)
            } else {
                syncStatusByTagNo[tagNo] = 2 // 不需同步
            }
        }
        if (eligibleTags.isEmpty()) {
            return U8SyncResult(statusMap = syncStatusByTagNo)
        }
        // 针对符合条件的标签执行 U8 推送
        errorMsg = trySyncStockMove(eligibleTags, orderMap, originLocMap, stockMove, targetLocArchive)
        val status = if (errorMsg == null) 1 else 0
        eligibleTags.forEach { syncStatusByTagNo[it.tagNo] = status }

        return U8SyncResult(
            statusMap = syncStatusByTagNo,
            failCount = syncStatusByTagNo.count { it.value == 0 },
            errorMessage = errorMsg
        )
    }

    /**
     * 发货出库改为推 U8 货位调整单，不传目标库位。
     * 转入货位只保留同批次未出库的货位。例如批次在开料01、货架01，出库货架01，转入货位为开料01。
     */
    fun syncShipStockMove(
        resolved: ResolvedTags,
        originLocMap: Map<String, StockInventory>,
        stockOut: StockOut
    ): U8SyncResult {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return U8SyncResult(statusMap = resolved.prodTags.associate { it.tagNo to 2 })
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }
        val tagMap = resolved.prodTags.associateBy { it.tagNo }
        val syncStatusByTagNo = mutableMapOf<String, Int>()

        val eligibleTags = mutableListOf<VProdTag>()
        resolved.tagNos.forEach { tagNo ->
            val prodTag = tagMap[tagNo]
            val order = prodTag?.prodOrderId?.let { orderMap[it] }
            if (order != null && (order.type == 2 || order.type == 0)) {
                eligibleTags.add(prodTag)
            } else {
                syncStatusByTagNo[tagNo] = 2
            }
        }
        if (eligibleTags.isEmpty()) {
            return U8SyncResult(statusMap = syncStatusByTagNo)
        }

        val errorMsg = trySyncShipStockMove(eligibleTags, orderMap, originLocMap, stockOut)
        val status = if (errorMsg == null) 1 else 0
        eligibleTags.forEach { syncStatusByTagNo[it.tagNo] = status }
        return U8SyncResult(
            statusMap = syncStatusByTagNo,
            failCount = syncStatusByTagNo.count { it.value == 0 },
            errorMessage = errorMsg
        )
    }

    private fun trySyncShipStockMove(
        eligibleTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        originLocMap: Map<String, StockInventory>,
        stockOut: StockOut
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val batches = eligibleTags.mapNotNull { originLocMap[it.tagNo]?.u8Batch }
                .filter { it.isNotBlank() }
                .distinct()
            val stocksByBatch = stockInventoryPlusService.listByU8Batches(batches).groupBy { it.u8Batch }

            val detailList = eligibleTags.groupBy { prodTag ->
                val order = orderMap[prodTag.prodOrderId]
                val invCode = order?.inventoryCode ?: prodTag.inventoryCode ?: ""
                val batch = originLocMap[prodTag.tagNo]?.u8Batch ?: ""
                invCode to batch
            }.values.mapIndexed { index, tagGroup ->
                val firstTag = tagGroup.first()
                val order = orderMap[firstTag.prodOrderId]
                val originStock = originLocMap[firstTag.tagNo]
                val shippedTagNos = tagGroup.map { it.tagNo }.toSet()
                val batchStocks = originStock?.u8Batch
                    ?.takeIf { it.isNotBlank() }
                    ?.let { stocksByBatch[it] }
                    .orEmpty()
                    .ifEmpty { originLocMap.filterKeys { it in shippedTagNos }.values.toList() }

                U8StockMoveDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = order?.inventoryCode ?: firstTag.inventoryCode
                    invPositionOut = LocUtil.joinLocCodesById(batchStocks.map { it.locId to it.locCode })
                    invPositionIn = LocUtil.joinLocCodesById(
                        batchStocks.filter { it.tagNo !in shippedTagNos }.map { it.locId to it.locCode }
                    )
                    packingMethod = order?.packingRequirement
                    specWidth = order?.specWidth?.takeIf { it.isNotBlank() } ?: "0"
                    batchNo = originStock?.u8Batch ?: order?.prodNo
                    rowMemo = tagGroup.joinToString(",") { it.tagNo }
                }
            }

            val u8Request = U8StockMoveRequest().apply {
                orderCode = stockOut.receiptNo
                orderDate = todayStr
                warehouseCode = "01"
                handler = "曾伟生"
                verifyDate = todayStr
                maker = stockOut.realName ?: stockOut.username
                memo = stockOut.type
                details = detailList
            }

            val response = u8StockMoveService.pushStockMove(u8Request)
            if (response.isSuccess) {
                log.info("U8 发货货位调整推送成功: receiptNo={}, 明细数量={}", stockOut.receiptNo, detailList.size)
                null
            } else {
                log.error("U8 发货货位调整推送失败: receiptNo={}, 原因={}", stockOut.receiptNo, response.returnMessage)
                response.returnMessage ?: "U8发货货位调整推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 发货货位调整接口调用异常: receiptNo={}", stockOut.receiptNo, e)
            "U8发货货位调整接口调用异常: ${e.message}"
        }
    }

    private fun trySyncStockMove(
        eligibleTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        originLocMap: Map<String, StockInventory>,
        stockMove: StockMove,
        targetLocArchive: LocArchive
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val targetLoc = targetLocArchive.locCode
            val batches = eligibleTags.mapNotNull { originLocMap[it.tagNo]?.u8Batch }
                .filter { it.isNotBlank() }
                .distinct()
            val stocksByBatch = stockInventoryPlusService.listByU8Batches(batches).groupBy { it.u8Batch }

            val detailList = eligibleTags.groupBy { prodTag ->
                val order = orderMap[prodTag.prodOrderId]
                val invCode = order?.inventoryCode ?: prodTag.inventoryCode ?: ""
                val batch = originLocMap[prodTag.tagNo]?.u8Batch ?: ""
                invCode to batch
            }.values.mapIndexed { index, tagGroup ->
                val firstTag = tagGroup.first()
                val order = orderMap[firstTag.prodOrderId]
                val originStock = originLocMap[firstTag.tagNo]
                val movedTagNos = tagGroup.map { it.tagNo }.toSet()
                val batchStocks = originStock?.u8Batch
                    ?.takeIf { it.isNotBlank() }
                    ?.let { stocksByBatch[it] }
                    .orEmpty()
                    .ifEmpty { originLocMap.filterKeys { it in movedTagNos }.values.toList() }

                U8StockMoveDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = order?.inventoryCode ?: firstTag.inventoryCode
                    invPositionOut = LocUtil.joinLocCodesById(batchStocks.map { it.locId to it.locCode })
                    invPositionIn = LocUtil.joinLocCodesById(
                        batchStocks.map { stock ->
                            if (stock.tagNo in movedTagNos) {
                                targetLocArchive.id to targetLoc
                            } else {
                                stock.locId to stock.locCode
                            }
                        }
                    )
                    packingMethod = order?.packingRequirement
                    specWidth = order?.specWidth
                    batchNo = originStock?.u8Batch ?: order?.prodNo
                    rowMemo = tagGroup.joinToString(",") { it.tagNo }
                }
            }

            val u8Request = U8StockMoveRequest().apply {
                orderCode = stockMove.receiptNo
                orderDate = todayStr
                warehouseCode = stockMove.warehouseCode?.takeIf { it.isNotBlank() } ?: "01"
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockMove.realName ?: stockMove.username
                memo = stockMove.type
                details = detailList

            }

            val response = u8StockMoveService.pushStockMove(u8Request)

            if (response.isSuccess) {
                log.info("U8 移库作业推送成功: receiptNo={}, 明细数量={}", stockMove.receiptNo, detailList.size)
                null
            } else {
                log.error("U8 移库作业推送失败: receiptNo={}, 原因={}", stockMove.receiptNo, response.returnMessage)
                response.returnMessage ?: "U8移库作业推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 移库作业接口调用异常: receiptNo={}", stockMove.receiptNo, e)
            "U8移库作业接口调用异常: ${e.message}"
        }
    }
}
