package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
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

        // 默认标记为 2 (不需同步)
        prodOrderIds.forEach { orderId ->
            syncStatusByOrderId[orderId] = 2
        }

        // 1. 同步采购入库 (type == 1 且有 erpOrderId)
        val purchaseTags = resolved.prodTags.filter { tag ->
            val po = orderMap[tag.prodOrderId]
            po != null && po.type == 1 && !po.erpOrderId.isNullOrBlank()
        }
        if (purchaseTags.isNotEmpty()) {
            val po = orderMap[purchaseTags.first().prodOrderId]!!
            val errorMsg = syncSinglePurchaseOrder(
                purchaseTags, po, stockIn, locArchive,
                purchaseTags.associate { it.tagNo to u8Batch }
            )
            val status = if (errorMsg == null) 1 else 0
            purchaseTags.mapNotNull { it.prodOrderId }.distinct().forEach { syncStatusByOrderId[it] = status }
            if (errorMsg != null) errors.add(errorMsg)
        }

        // 2. 成品入库包含两类标签：
        //    A. 生产订单直接入库 (type == 2 且有 erpOrderId)
        //    B. 销售订单入库 (type == 0)，需根据 inventoryCode 反查最新的有效生产订单 (type == 2)
        val momDirectTags = resolved.prodTags.filter { tag ->
            val po = orderMap[tag.prodOrderId]
            po != null && po.type == 2 && !po.erpOrderId.isNullOrBlank()
        }

        val salesTags = resolved.prodTags.filter { tag ->
            val po = orderMap[tag.prodOrderId]
            po != null && po.type == 0
        }

        // 处理销售订单标签：按存货编码与客户编号反查生产单
        val momResolvedOrdersByTag = mutableMapOf<String, ProdOrder>()
        val salesInvCodeCache = mutableMapOf<String, ProdOrder>()

        salesTags.forEach { tag ->
            val so = orderMap[tag.prodOrderId]
            val invCode = so?.inventoryCode ?: tag.inventoryCode ?: ""
            val customerCode = so?.customerCode
            if (invCode.isBlank()) {
                throw WebException("标签条码[${tag.tagNo}]缺少存货编码，无法反查生产订单推送到U8")
            }
            val cacheKey = "$invCode:${customerCode ?: ""}"
            val momOrder = salesInvCodeCache.getOrPut(cacheKey) {
                productionOrderPlusService.findLatestMomOrderByInvCodeAndCustomerCode(invCode, customerCode)
                    ?: throw WebException("存货[${invCode}]${if (!customerCode.isNullOrBlank()) "、客户[${customerCode}]" else ""}未找到对应的有效生产订单(已下发U8)，无法回传U8成品入库")
            }
            momResolvedOrdersByTag[tag.tagNo] = momOrder
        }

        // 合并所有成品入库标签：直接生产单 + 销售单反查生产单
        val allMomTags = momDirectTags + salesTags
        if (allMomTags.isNotEmpty()) {
            val firstMomOrder = if (momDirectTags.isNotEmpty()) {
                orderMap[momDirectTags.first().prodOrderId]!!
            } else {
                momResolvedOrdersByTag[salesTags.first().tagNo]!!
            }
            val errorMsg = syncSingleMomOrder(
                allMomTags, firstMomOrder, stockIn, locArchive,
                allMomTags.associate { it.tagNo to u8Batch }
            )
            val status = if (errorMsg == null) 1 else 0
            allMomTags.mapNotNull { it.prodOrderId }.distinct().forEach { syncStatusByOrderId[it] = status }
            if (errorMsg != null) errors.add(errorMsg)
        }

        return U8SyncResult(
            statusMap = syncStatusByOrderId,
            failCount = syncStatusByOrderId.count { it.value == 0 },
            errorMessage = errors.joinToString("; ").ifBlank { null }
        )
    }

    /**
     * 同步单张采购入库单（严格属于同一个采购单 erpOrderId 的标签集合）
     * 一箱一行明细，每箱独立批次号。
     * @return 错误信息，若成功返回 null
     */
    fun syncSinglePurchaseOrder(
        purchaseTags: List<VProdTag>,
        prodOrder: ProdOrder,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8BatchByTagNo: Map<String, String>
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val detailList = purchaseTags.mapIndexed { index, tag ->
                U8PurchaseStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = tag.inventoryCode ?: prodOrder.inventoryCode ?: ""
                    quantity = BigDecimal.valueOf((tag.qty ?: 0).toLong())
                    auxiliaryQuantity = tagAuxiliaryQty(tag)
                    purchaseOrderDetailId = prodOrder.erpOrderId ?: ""
                    invPosition = locArchive.locCode
                    batchNo = u8BatchByTagNo[tag.tagNo] ?: ""
                    rowMemo = tag.tagNo
                }
            }

            val u8Request = U8PurchaseStockInRequest().apply {
                orderCode = stockIn.receiptNo
                orderDate = todayStr
                warehouseCode = "01"
                handler = "曾伟生"
                verifyDate = todayStr
                maker = stockIn.realName ?: stockIn.username
                details = detailList
            }

            val response = u8PurchaseStockInService.pushPurchaseStockIn(u8Request)

            if (response.isSuccess) {
                log.info("U8 采购入库推送成功: receiptNo={}, erpOrderId={}, 明细行数={}", stockIn.receiptNo, prodOrder.erpOrderId, detailList.size)
                null
            } else {
                log.error("U8 采购入库推送失败: receiptNo={}, erpOrderId={}, 原因={}", stockIn.receiptNo, prodOrder.erpOrderId, response.returnMessage)
                response.returnMessage ?: "U8采购入库推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 采购入库接口调用异常: receiptNo={}, erpOrderId={}", stockIn.receiptNo, prodOrder.erpOrderId, e)
            "U8采购入库接口调用异常: ${e.message}"
        }
    }

    /**
     * 同步单张产成品入库单（严格属于同一个生产单 erpOrderId 的标签集合，包含销售单反查绑定的生产单）
     * 一箱一行明细，每箱独立批次号。
     * @return 错误信息，若成功返回 null
     */
    fun syncSingleMomOrder(
        momTags: List<VProdTag>,
        momOrder: ProdOrder,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8BatchByTagNo: Map<String, String>
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            val detailList = momTags.mapIndexed { index, tag ->
                U8MomStockInDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = tag.inventoryCode ?: momOrder.inventoryCode ?: ""
                    quantity = BigDecimal.valueOf((tag.qty ?: 0).toLong())
                    auxiliaryQuantity = tagAuxiliaryQty(tag)
                    momOrderDetailId = momOrder.erpOrderId ?: ""
                    invPosition = locArchive.locCode
                    batchNo = u8BatchByTagNo[tag.tagNo] ?: ""
                    packingMethod = momOrder.packingRequirement
                    specWidth = momOrder.specWidth
                    rowMemo = tag.tagNo
                }
            }

            val u8Request = U8MomStockInRequest().apply {
                orderCode = stockIn.receiptNo
                orderDate = todayStr
                rdCode = "102"
                warehouseCode = "01"
                handler = "曾伟生"
                verifyDate = todayStr
                maker = stockIn.realName ?: stockIn.username
                details = detailList
            }

            val response = u8MomStockInService.pushMomStockIn(u8Request)
            if (response.isSuccess) {
                log.info("U8 产成品入库推送成功: receiptNo={}, erpOrderId={}, 明细行数={}", stockIn.receiptNo, momOrder.erpOrderId, detailList.size)
                null
            } else {
                log.error("U8 产成品入库推送失败: receiptNo={}, erpOrderId={}, 原因={}", stockIn.receiptNo, momOrder.erpOrderId, response.returnMessage)
                response.returnMessage ?: "U8产成品入库推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 产成品入库接口调用异常: receiptNo={}, erpOrderId={}", stockIn.receiptNo, momOrder.erpOrderId, e)
            "U8产成品入库接口调用异常: ${e.message}"
        }
    }

    /**
     * 单批次入库同步入口：根据 targetType 判断调用采购入库或产成品入库。
     */
    fun syncSingleBatch(
        resolved: ResolvedTags,
        stockIn: StockIn,
        locArchive: LocArchive,
        u8BatchByTagNo: Map<String, String>,
        targetType: Int,
        targetErpOrderId: String,
        momOrderMap: Map<String, ProdOrder>
    ): U8SyncResult {
        val prodTags = resolved.prodTags
        val errorMsg: String?

        if (targetType == 1) {
            val dummyPo = ProdOrder().apply {
                this.erpOrderId = targetErpOrderId
                this.inventoryCode = prodTags.firstOrNull()?.inventoryCode
                this.inNum = null
            }
            errorMsg = syncSinglePurchaseOrder(prodTags, dummyPo, stockIn, locArchive, u8BatchByTagNo)
        } else {
            val momOrder = momOrderMap.values.firstOrNull { it.erpOrderId == targetErpOrderId }
                ?: ProdOrder().apply {
                    this.erpOrderId = targetErpOrderId
                    this.inventoryCode = prodTags.firstOrNull()?.inventoryCode
                }
            errorMsg = syncSingleMomOrder(prodTags, momOrder, stockIn, locArchive, u8BatchByTagNo)
        }

        val status = if (errorMsg == null) 1 else 0
        val statusMap = prodTags.associate { it.tagNo to status }

        return U8SyncResult(
            statusMap = statusMap,
            failCount = if (errorMsg == null) 0 else prodTags.size,
            errorMessage = errorMsg
        )
    }

    private fun tagAuxiliaryQty(tag: VProdTag): BigDecimal {
        val net = tag.netWeight
        return if (net == null || net.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ONE else net
    }
}
