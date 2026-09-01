package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.dto.U8MomStockInDetailRequest
import com.gz.xg.u8.dto.U8MomStockInRequest
import com.gz.xg.u8.dto.U8PurchaseStockInDetailRequest
import com.gz.xg.u8.dto.U8PurchaseStockInRequest
import com.gz.xg.u8.service.U8MomStockInService
import com.gz.xg.u8.service.U8PurchaseStockInService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 入库单同步/回传用友 U8 业务服务。
 * 统一管理采购入库、产成品入库等各种入库单据推送到 U8 的业务组装与接口调用。
 */
@Service
class U8StockInSyncService(
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val u8PurchaseStockInService: U8PurchaseStockInService,
    private val u8MomStockInService: U8MomStockInService
) : BaseService() {

    /**
     * 同步入库单到 U8，返回每个 prodOrderId 对应的 u8Sync 状态（0-未同步, 1-已同步, 2-不需同步）及 U8 错误信息
     */
    fun syncStockIn(
        resolved: ResolvedTags,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8Batch : String
    ): U8SyncResult {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return U8SyncResult()
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }

        // 最终返回每个 prodOrderId 的同步状态
        val syncStatusByOrderId = mutableMapOf<String, Int>()
        val errors = mutableListOf<String>()

        // 标记不需要同步的订单（type != 1 且 != 2，或者无 erpOrderId）
        prodOrderIds.forEach { orderId ->
            val order = orderMap[orderId]
            if (order == null || order.erpOrderId.isNullOrBlank() || (order.type != 1 && order.type != 2)) {
                syncStatusByOrderId[orderId] = 2 // 不需同步
            }
        }

        // 1. 同步采购入库 (type == 1 且有 erpOrderId)
        val purchaseTags = resolved.prodTags.filter { tag ->
            val po = orderMap[tag.prodOrderId]
            po != null && po.type == 1 && !po.erpOrderId.isNullOrBlank()
        }
        if (purchaseTags.isNotEmpty()) {
            val errorMsg = trySyncPurchaseStockIn(purchaseTags, orderMap, stockIn, locArchive, u8Batch)
            val status = if (errorMsg == null) 1 else 0
            purchaseTags.mapNotNull { it.prodOrderId }.distinct().forEach { syncStatusByOrderId[it] = status }
            if (errorMsg != null) errors.add(errorMsg)
        }

        // 2. 同步产成品入库 (type == 2 且有 erpOrderId)
        val momTags = resolved.prodTags.filter { tag ->
            val po = orderMap[tag.prodOrderId]
            po != null && po.type == 2 && !po.erpOrderId.isNullOrBlank()
        }
        if (momTags.isNotEmpty()) {
            val errorMsg = trySyncMomStockIn(momTags, orderMap, stockIn, locArchive, u8Batch)
            val status = if (errorMsg == null) 1 else 0
            momTags.mapNotNull { it.prodOrderId }.distinct().forEach { syncStatusByOrderId[it] = status }
            if (errorMsg != null) errors.add(errorMsg)
        }

        return U8SyncResult(
            statusMap = syncStatusByOrderId,
            failCount = syncStatusByOrderId.count { it.value == 0 },
            errorMessage = errors.joinToString("; ").ifBlank { null }
        )
    }

    /**
     * 尝试同步采购入库，每个 ProdTag 对应一条明细，quantity 取 ProdTag.qty
     */
    private fun trySyncPurchaseStockIn(
        purchaseTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8Batch: String
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 每个 ProdTag 对应一条 U8 明细
            val detailList = purchaseTags.mapIndexed { index, prodTag ->
                val po = orderMap[prodTag.prodOrderId]
                val tagQty = BigDecimal.valueOf(prodTag.qty.toLong())

                U8PurchaseStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = po?.inventoryCode ?: prodTag.inventoryCode ?: ""
                    quantity = tagQty                  // 单个 ProdTag.qty
                    auxiliaryQuantity = po?.inNum      // ProdOrder.inNum
                    purchaseOrderDetailId = po?.erpOrderId ?: ""
                    invPosition = locArchive.locCode
                    batchNo = u8Batch
                }
            }

            val u8Request = U8PurchaseStockInRequest().apply {
                orderCode = stockIn.receiptNo
                orderDate = todayStr
                warehouseCode = "01"                  // 固定为 01
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockIn.realName ?: stockIn.username
                details = detailList
            }

            val response = u8PurchaseStockInService.pushPurchaseStockIn(u8Request)
            if (response.isSuccess) {
                log.info("U8 采购入库推送成功: receiptNo={}, 明细行数={}", stockIn.receiptNo, detailList.size)
                null
            } else {
                log.error("U8 采购入库推送失败: receiptNo={}, 原因={}", stockIn.receiptNo, response.returnMessage)
                response.returnMessage ?: "U8采购入库推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 采购入库接口调用异常: receiptNo={}", stockIn.receiptNo, e)
            "U8采购入库接口调用异常: ${e.message}"
        }
    }

    /**
     * 尝试同步产成品入库，每个 ProdTag 对应一条明细，quantity 取 ProdTag.qty
     */
    private fun trySyncMomStockIn(
        momTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8Batch : String
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 每个 ProdTag 对应一条 U8 明细
            val detailList = momTags.mapIndexed { index, prodTag ->
                val po = orderMap[prodTag.prodOrderId]
                val tagQty = BigDecimal.valueOf(prodTag.qty.toLong())

                U8MomStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = po?.inventoryCode ?: prodTag.inventoryCode ?: ""
                    quantity = tagQty                  // 单个 ProdTag.qty
                    auxiliaryQuantity = if (po?.inNum == null || po.inNum.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ONE else po.inNum
                    momOrderDetailId = po?.erpOrderId ?: ""   // ProdOrder.erpOrderId
                    invPosition = locArchive.locCode
                    batchNo = u8Batch
                    packingMethod = po?.packingRequirement
                    specWidth = po?.specWidth
                }
            }

            val u8Request = U8MomStockInRequest().apply {
                orderCode = stockIn.receiptNo
                orderDate = todayStr
                rdCode = "102"                         // 固定为 102
                warehouseCode = "01"                  // 固定为 01
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockIn.realName ?: stockIn.username
                details = detailList
            }

            val response = u8MomStockInService.pushMomStockIn(u8Request)
            if (response.isSuccess) {
                log.info("U8 产成品入库推送成功: receiptNo={}, 明细行数={}", stockIn.receiptNo, detailList.size)
                null
            } else {
                log.error("U8 产成品入库推送失败: receiptNo={}, 原因={}", stockIn.receiptNo, response.returnMessage)
                response.returnMessage ?: "U8产成品入库推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 产成品入库接口调用异常: receiptNo={}", stockIn.receiptNo, e)
            "U8产成品入库接口调用异常: ${e.message}"
        }
    }
}
