package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.dto.U8MomStockInDetailRequest
import com.gz.xg.u8.dto.U8MomStockInRequest
import com.gz.xg.u8.dto.U8PurchaseStockInDetailRequest
import com.gz.xg.u8.dto.U8PurchaseStockInRequest
import com.gz.xg.u8.service.U8MomStockInService
import com.gz.xg.u8.service.U8PurchaseStockInService
import org.springframework.stereotype.Service
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
     * 统一同步入库单数据至 U8
     * 根据关联 ProdOrder 的类型分别处理：
     * - type == 1: 采购入库 (Rdrecord01)
     * - type == 2: 产成品入库 (Rdrecord10)
     */
    fun syncStockIn(
        resolved: ResolvedTags,
        stockIn: StockIn,
        locArchive: LocArchive
    ) {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) return

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
            .filter { !it.erpOrderId.isNullOrBlank() }

        if (orders.isEmpty()) return

        // 1. 同步采购入库 (type == 1)
        val purchaseOrders = orders.filter { it.type == 1 }
        if (purchaseOrders.isNotEmpty()) {
            syncPurchaseStockIn(purchaseOrders, stockIn, locArchive)
        }

        // 2. 同步产成品入库 (type == 2)
        val momOrders = orders.filter { it.type == 2 }
        if (momOrders.isNotEmpty()) {
            syncMomStockIn(momOrders, stockIn, locArchive)
        }
    }

    /**
     * 同步入库单数据至 U8 采购入库
     * 规则：
     * 1. 过滤关联 ProdOrder.type == 1 且具有 erpOrderId 的采购订单
     * 2. warehouseCode (cwhcode) 固定为 "01"
     * 3. handler (chandler) 固定为 "曾伟生"
     * 4. quantity (iquantity) 取 ProdOrder.qty
     * 5. auxiliaryQuantity (inum) 取 ProdOrder.inNum
     * 6. 强一致性校验：推送失败抛出 WebException 触发本地事务回滚
     */
    private fun syncPurchaseStockIn(
        purchaseOrders: List<com.gz.xg.domain.entity.ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive
    ) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // 组装采购入库明细
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

        // 组装采购入库主单
        val u8Request = U8PurchaseStockInRequest().apply {
            orderCode = stockIn.receiptNo
            orderDate = todayStr
            warehouseCode = "01"                  // 固定为 01
            handler = "曾伟生"                    // 固定为 曾伟生
            verifyDate = todayStr
            maker = stockIn.realName ?: stockIn.username
            details = detailList
        }

        // 发送 U8 采购入库推送
        val response = u8PurchaseStockInService.pushPurchaseStockIn(u8Request)
        if (!response.isSuccess) {
            val errorMsg = response.returnMessage ?: "未知错误"
            log.error("推送 U8 采购入库失败: receiptNo={}, 原因={}", stockIn.receiptNo, errorMsg)
            throw WebException("U8采购入库推送失败: $errorMsg")
        }
    }

    /**
     * 同步入库单数据至 U8 产成品入库
     * 规则：
     * 1. 过滤关联 ProdOrder.type == 2 且具有 erpOrderId 的生产订单
     * 2. rdCode (crdcode) 固定为 "10"
     * 3. warehouseCode (cwhcode) 固定为 "01"
     * 4. handler (chandler) 固定为 "曾伟生"
     * 5. quantity (iquantity) 取 ProdOrder.qty
     * 6. auxiliaryQuantity (inum) 取 ProdOrder.inNum
     * 7. momOrderDetailId (modid) 取 ProdOrder.erpOrderId
     * 8. 强一致性校验：推送失败抛出 WebException 触发本地事务回滚
     */
    private fun syncMomStockIn(
        momOrders: List<com.gz.xg.domain.entity.ProdOrder>,
        stockIn: StockIn,
        locArchive: LocArchive
    ) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        // 组装产成品入库明细
        val detailList = momOrders.mapIndexed { index, po ->
            U8MomStockInDetailRequest().apply {
                rowNo = index + 1
                inventoryCode = po.inventoryCode
                quantity = po.qty                  // ProdOrder.qty
                auxiliaryQuantity = po.inNum       // ProdOrder.inNum
                momOrderDetailId = po.erpOrderId   // ProdOrder.erpOrderId
                invPosition = locArchive.locCode
            }
        }

        // 组装产成品入库主单
        val u8Request = U8MomStockInRequest().apply {
            orderCode = stockIn.receiptNo
            orderDate = todayStr
            rdCode = "10"                         // 固定为 10
            warehouseCode = "01"                  // 固定为 01
            handler = "曾伟生"                    // 固定为 曾伟生
            verifyDate = todayStr
            maker = stockIn.realName ?: stockIn.username
            details = detailList
        }

        // 发送 U8 产成品入库推送
        val response = u8MomStockInService.pushMomStockIn(u8Request)
        if (!response.isSuccess) {
            val errorMsg = response.returnMessage ?: "未知错误"
            log.error("推送 U8 产成品入库失败: receiptNo={}, 原因={}", stockIn.receiptNo, errorMsg)
            throw WebException("U8产成品入库推送失败: $errorMsg")
        }
    }
}
