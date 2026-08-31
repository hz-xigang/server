package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.dto.U8SalesStockOutDetailRequest
import com.gz.xg.u8.dto.U8SalesStockOutRequest
import com.gz.xg.u8.service.U8SalesStockOutService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 出库业务同步 U8 成品出仓（销售出库）服务。
 */
@Service
class U8SalesStockOutSyncService(
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val u8SalesStockOutService: U8SalesStockOutService
) : BaseService() {

    /**
     * 同步出库数据至 U8 成品出仓单。
     * 每个标签一条明细，仅对 ProdOrder.type == 2 且有 erpOrderId 的标签发起推送。
     *
     * @param resolved 解析后的标签结果
     * @param stockOut 出库主单
     * @param locCodeByTagNo 每个标签对应的出库货位映射 (tagNo -> locCode)
     * @return 每个 tagNo 对应的 u8Sync 状态（0-未同步, 1-已同步, 2-不需同步）
     */
    fun syncSalesStockOut(
        resolved: ResolvedTags,
        stockOut: StockOut,
        locCodeByTagNo: Map<String, String>
    ): U8SyncResult {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return U8SyncResult(statusMap = resolved.prodTags.associate { it.tagNo to 2 })
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }

        val tagMap = resolved.prodTags.associateBy { it.tagNo }
        val syncStatusByTagNo = mutableMapOf<String, Int>()

        // 筛选出 type == 2 且有 erpOrderId 的标签项
        val eligibleTags = mutableListOf<VProdTag>()
        resolved.tagNos.forEach { tagNo ->
            val prodTag = tagMap[tagNo]
            val order = prodTag?.prodOrderId?.let { orderMap[it] }
            if (order != null && order.type == 2 && !order.erpOrderId.isNullOrBlank()) {
                eligibleTags.add(prodTag)
            } else {
                syncStatusByTagNo[tagNo] = 2 // 不需同步
            }
        }

        if (eligibleTags.isEmpty()) {
            return U8SyncResult(statusMap = syncStatusByTagNo)
        }

        // 针对 type == 2 的标签执行 U8 推送
        val errorMsg = trySyncSalesStockOut(eligibleTags, orderMap, stockOut, locCodeByTagNo)
        val status = if (errorMsg == null) 1 else 0
        eligibleTags.forEach { syncStatusByTagNo[it.tagNo] = status }

        return U8SyncResult(
            statusMap = syncStatusByTagNo,
            failCount = syncStatusByTagNo.count { it.value == 0 },
            errorMessage = errorMsg
        )
    }

    private fun trySyncSalesStockOut(
        eligibleTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        stockOut: StockOut,
        locCodeByTagNo: Map<String, String>
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 每个标签对应一条出库明细
            val detailList = eligibleTags.mapIndexed { index, prodTag ->
                val order = orderMap[prodTag.prodOrderId]

                U8SalesStockOutDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = order?.inventoryCode ?: ""
                    inventoryName = order?.inventoryName ?: ""
                    spec = order?.spec ?: ""
                    material = order?.material ?: ""
                    packingMethod = order?.packingRequirement ?: ""
                    specWidth = order?.specWidth ?: ""
                    customerCode = order?.customerCode ?: ""
                    unitWeight = order?.unitWeight
                    totalWeight = BigDecimal.ZERO
                    invAddCode = ""
                    unitName = ""
                    auxiliaryQuantity = order?.inNum
                    quantity = order?.qty
                    invPosition = locCodeByTagNo[prodTag.tagNo] ?: ""
                    batchNo = order?.prodNo ?: ""
                    rowMemo = prodTag.tagNo
                    salesOrderDetailId = order?.erpOrderId ?: ""
                }
            }

            val u8Request = U8SalesStockOutRequest().apply {
                orderCode = stockOut.receiptNo
                orderDate = todayStr
                warehouseCode = "01"                  // 固定为 01
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockOut.realName ?: stockOut.username
                memo = stockOut.type
                details = detailList
            }

            val response = u8SalesStockOutService.pushSalesStockOut(u8Request)
            if (response.isSuccess) {
                log.info("U8 成品出仓推送成功: receiptNo={}, 明细数量={}", stockOut.receiptNo, detailList.size)
                null
            } else {
                log.error("U8 成品出仓推送失败: receiptNo={}, 原因={}", stockOut.receiptNo, response.returnMessage)
                response.returnMessage ?: "U8成品出仓推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 成品出仓接口调用异常: receiptNo={}", stockOut.receiptNo, e)
            "U8成品出仓接口调用异常: ${e.message}"
        }
    }
}
