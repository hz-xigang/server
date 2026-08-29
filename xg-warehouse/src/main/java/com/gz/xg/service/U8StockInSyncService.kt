package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.dto.U8MomStockInDetailRequest
import com.gz.xg.u8.dto.U8MomStockInRequest
import com.gz.xg.u8.dto.U8PurchaseStockInDetailRequest
import com.gz.xg.u8.dto.U8PurchaseStockInRequest
import com.gz.xg.u8.service.U8MomStockInService
import com.gz.xg.u8.service.U8PurchaseStockInService
import com.gz.xg.util.IdUtil
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
     * 同步入库单到 U8，返回每个 prodOrderId 对应的 u8Sync 状态（0-未同步, 1-已同步, 2-不需同步）
     */
    fun syncStockIn(
        resolved: ResolvedTags,
        stockIn: StockIn,
        locArchive: LocArchive
    ): Map<String, Int> {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return emptyMap()
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }

        // 最终返回每个 prodOrderId 的同步状态
        val syncStatusByOrderId = mutableMapOf<String, Int>()

        // 标记不需要同步的订单（type != 1 且 != 2，或者无 erpOrderId）
        prodOrderIds.forEach { orderId ->
            val order = orderMap[orderId]
            if (order == null || order.erpOrderId.isNullOrBlank() || (order.type != 1 && order.type != 2)) {
                syncStatusByOrderId[orderId] = 2 // 不需同步
            }
        }

        // 1. 同步采购入库 (type == 1 且有 erpOrderId)
        val purchaseOrders = orders.filter { it.type == 1 && !it.erpOrderId.isNullOrBlank() }
        if (purchaseOrders.isNotEmpty()) {
            val success = trySyncPurchaseStockIn(purchaseOrders, stockIn, locArchive)
            val status = if (success) 1 else 0
            purchaseOrders.forEach { syncStatusByOrderId[it.id] = status }
        }

        // 2. 同步产成品入库 (type == 2 且有 erpOrderId)
        val momOrders = orders.filter { it.type == 2 && !it.erpOrderId.isNullOrBlank() }
        if (momOrders.isNotEmpty()) {
            val success = trySyncMomStockIn(momOrders, stockIn, locArchive)
            val status = if (success) 1 else 0
            momOrders.forEach { syncStatusByOrderId[it.id] = status }
        }

        return syncStatusByOrderId
    }

    /**
     * 尝试同步采购入库，异常与失败不中断主流程，返回 true 表示成功，false 表示失败
     */
    private fun trySyncPurchaseStockIn(
        purchaseOrders: List<com.gz.xg.domain.entity.ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive
    ): Boolean {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val detailList = purchaseOrders.mapIndexed { index, po ->
                U8PurchaseStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = po.inventoryCode
                    quantity = po.qty                  // ProdOrder.qty
                    auxiliaryQuantity = po.inNum       // ProdOrder.inNum
                    purchaseOrderDetailId = po.erpOrderId
                    invPosition = locArchive.locCode
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
                log.info("U8 采购入库推送成功: receiptNo={}", stockIn.receiptNo)
                true
            } else {
                log.error("U8 采购入库推送失败: receiptNo={}, 原因={}", stockIn.receiptNo, response.returnMessage)
                false
            }
        } catch (e: Exception) {
            log.error("U8 采购入库接口调用异常: receiptNo={}", stockIn.receiptNo, e)
            false
        }
    }

    /**
     * 尝试同步产成品入库，异常与失败不中断主流程，返回 true 表示成功，false 表示失败
     */
    private fun trySyncMomStockIn(
        momOrders: List<com.gz.xg.domain.entity.ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive
    ): Boolean {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val detailList = momOrders.mapIndexed { index, po ->
                U8MomStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = po.inventoryCode
                    quantity = po.qty                  // ProdOrder.qty
                    auxiliaryQuantity = if(po.inNum == null || po.inNum.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ONE else po.inNum         // ProdOrder.inNum
                    momOrderDetailId = po.erpOrderId   // ProdOrder.erpOrderId
                    invPosition = locArchive.locCode
                    batchNo = IdUtil.generateId()
                    packingMethod = po.packingRequirement
                    specWidth = po.specWidth

                }
            }

            val u8Request = U8MomStockInRequest().apply {
                orderCode = stockIn.receiptNo
                orderDate = todayStr
                rdCode = "102"                         // 固定为 10
                warehouseCode = "01"                  // 固定为 01
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockIn.realName ?: stockIn.username
                details = detailList
            }

            val response = u8MomStockInService.pushMomStockIn(u8Request)
            if (response.isSuccess) {
                log.info("U8 产成品入库推送成功: receiptNo={}", stockIn.receiptNo)
                true
            } else {
                log.error("U8 产成品入库推送失败: receiptNo={}, 原因={}", stockIn.receiptNo, response.returnMessage)
                false
            }
        } catch (e: Exception) {
            log.error("U8 产成品入库接口调用异常: receiptNo={}", stockIn.receiptNo, e)
            false
        }
    }
}
