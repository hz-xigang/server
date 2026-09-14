package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.u8.dto.U8SalesStockOutDetailRequest
import com.gz.xg.u8.dto.U8SalesStockOutRequest
import com.gz.xg.u8.service.U8SalesStockOutService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 发货出库同步 U8 成品出仓（销售出库）服务。
 */
@Service
class U8SalesStockOutSyncService(
    private val u8SalesStockOutService: U8SalesStockOutService
) : BaseService() {

    /**
     * 同步发货出库数据至 U8 成品出仓单。
     * 以发货单 (ShipOrder/ShipOrderDetail) 数据为准构造推送明细。
     *
     * @param resolved 解析后的标签结果
     * @param stockOut 出库主单
     * @param shipOrder 发货单主表
     * @param orderDetails 发货单明细列表
     * @param locCodeByTagNo 每个标签对应的出库货位映射 (tagNo -> locCode)
     * @param u8BatchByTagNo 每个标签对应的 U8 批次号映射 (tagNo -> u8Batch)
     * @return 每个 tagNo 对应的 u8Sync 状态（0-未同步, 1-已同步, 2-不需同步）
     */
    fun syncSalesStockOut(
        resolved: ResolvedTags,
        stockOut: StockOut,
        shipOrder: ShipOrder,
        orderDetails: List<ShipOrderDetail>,
        locCodeByTagNo: Map<String, String>,
        u8BatchByTagNo: Map<String, String>
    ): U8SyncResult {
        if (resolved.prodTags.isEmpty()) {
            return U8SyncResult(statusMap = emptyMap())
        }

        val detailMap = orderDetails.associateBy { it.inventoryCode }
        val tagMap = resolved.prodTags.associateBy { it.tagNo }
        val syncStatusByTagNo = mutableMapOf<String, Int>()

        val eligibleTags = mutableListOf<VProdTag>()
        resolved.tagNos.forEach { tagNo ->
            val prodTag = tagMap[tagNo]
            if (prodTag != null && detailMap.containsKey(prodTag.inventoryCode)) {
                eligibleTags.add(prodTag)
            } else {
                syncStatusByTagNo[tagNo] = 2
            }
        }

        if (eligibleTags.isEmpty()) {
            return U8SyncResult(statusMap = syncStatusByTagNo)
        }

        val errorMsg = trySyncSalesStockOut(eligibleTags, detailMap, shipOrder, stockOut, locCodeByTagNo, u8BatchByTagNo)
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
        detailMap: Map<String, ShipOrderDetail>,
        shipOrder: ShipOrder,
        stockOut: StockOut,
        locCodeByTagNo: Map<String, String>,
        u8BatchByTagNo: Map<String, String>
    ): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 按存货编号分组汇总推送 U8 成品出仓单
            val groupedByInvCode = eligibleTags.groupBy { it.inventoryCode ?: "" }
            val detailList = groupedByInvCode.entries.mapIndexed { index, (_, tagGroup) ->
                val firstTag = tagGroup.first()
                val orderDetail = detailMap[firstTag.inventoryCode]
                val totalQty = tagGroup.sumOf { (it.qty ?: 0).toLong() }
                val totalGrossWeight = tagGroup.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.grossWeight ?: BigDecimal.ZERO) }
                val totalNetWeight = tagGroup.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.netWeight ?: BigDecimal.ZERO) }

                U8SalesStockOutDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = orderDetail?.inventoryCode ?: firstTag.inventoryCode ?: ""
                    inventoryName = orderDetail?.inventoryName ?: firstTag.inventoryName ?: ""
                    spec = orderDetail?.spec ?: firstTag.spec ?: ""
                    material = orderDetail?.material ?: ""
                    packingMethod = orderDetail?.packingMethod ?: ""
                    specWidth = orderDetail?.specWidth ?: ""
                    customerCode = shipOrder.customerCode ?: ""
                    unitWeight = orderDetail?.unitWeight ?: BigDecimal.ZERO
                    totalWeight = totalGrossWeight
                    invAddCode = ""
                    unitName = orderDetail?.unit ?: ""
                    auxiliaryQuantity = totalNetWeight
                    quantity = BigDecimal.valueOf(totalQty)
                    invPosition = locCodeByTagNo[firstTag.tagNo] ?: ""
                    // 批次号取库存表 StockInventory.u8Batch（以组内第一个标签为准）
                    batchNo = u8BatchByTagNo[firstTag.tagNo] ?: ""
                    // 行备注逗号拼接组内所有箱标签号
                    rowMemo = tagGroup.joinToString(",") { it.tagNo }
                    // isosid 销售订单行主键，存放在 m5
                    salesOrderDetailId = orderDetail?.m5 ?: ""
                }
            }

            val u8Request = U8SalesStockOutRequest().apply {
                orderCode = stockOut.receiptNo
                orderDate = todayStr
                warehouseCode = "01"
                handler = "曾伟生"
                verifyDate = todayStr
                maker = stockOut.realName ?: stockOut.username
                memo = shipOrder.m3 ?: stockOut.type
                details = detailList
            }

            val response = u8SalesStockOutService.pushSalesStockOut(u8Request)
            if (response.isSuccess) {
                log.info("U8 成品出仓推送成功: receiptNo={}, shipNo={}, 明细数量={}", stockOut.receiptNo, shipOrder.shipNo, detailList.size)
                null
            } else {
                log.error("U8 成品出仓推送失败: receiptNo={}, shipNo={}, 原因={}", stockOut.receiptNo, shipOrder.shipNo, response.returnMessage)
                response.returnMessage ?: "U8成品出仓推送失败"
            }
        } catch (e: Exception) {
            log.error("U8 成品出仓接口调用异常: receiptNo={}, shipNo={}", stockOut.receiptNo, shipOrder.shipNo, e)
            "U8成品出仓接口调用异常: ${e.message}"
        }
    }
}
