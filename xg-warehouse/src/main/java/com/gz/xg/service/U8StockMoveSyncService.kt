package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.dto.U8StockMoveDetailRequest
import com.gz.xg.u8.dto.U8StockMoveRequest
import com.gz.xg.u8.service.U8StockMoveService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 移库业务同步 U8 货位调整单服务。
 */
@Service
class U8StockMoveSyncService(
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val u8StockMoveService: U8StockMoveService
) : BaseService() {

    /**
     * 同步移库数据至 U8 货位调整单。
     * 每个标签一条明细，仅对 ProdOrder.type == 2 的标签发起推送。
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
    ): Map<String, Int> {
        val prodOrderIds = resolved.prodTags.mapNotNull { it.prodOrderId }.distinct()
        if (prodOrderIds.isEmpty()) {
            return resolved.prodTags.associate { it.tagNo to 2 }
        }

        val orders = productionOrderPlusService.listByIds(prodOrderIds)
        val orderMap = orders.associateBy { it.id }

        val tagMap = resolved.prodTags.associateBy { it.tagNo }
        val syncStatusByTagNo = mutableMapOf<String, Int>()

        // 筛选出 type == 2 的标签项
        val eligibleTags = mutableListOf<VProdTag>()
        resolved.tagNos.forEach { tagNo ->
            val prodTag = tagMap[tagNo]
            val order = prodTag?.prodOrderId?.let { orderMap[it] }
            if (order != null && order.type == 2) {
                eligibleTags.add(prodTag)
            } else {
                syncStatusByTagNo[tagNo] = 2 // 不需同步
            }
        }

        if (eligibleTags.isEmpty()) {
            return syncStatusByTagNo
        }

        // 针对 type == 2 的标签执行 U8 推送
        val success = trySyncStockMove(eligibleTags, orderMap, originLocMap, stockMove, targetLocArchive)
        val status = if (success) 1 else 0
        eligibleTags.forEach { syncStatusByTagNo[it.tagNo] = status }

        return syncStatusByTagNo
    }

    private fun trySyncStockMove(
        eligibleTags: List<VProdTag>,
        orderMap: Map<String, ProdOrder>,
        originLocMap: Map<String, StockInventory>,
        stockMove: StockMove,
        targetLocArchive: LocArchive
    ): Boolean {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            // 每个标签对应一条移库明细
            val detailList = eligibleTags.mapIndexed { index, prodTag ->
                val order = orderMap[prodTag.prodOrderId]
                val originStock = originLocMap[prodTag.tagNo]

                U8StockMoveDetailRequest().apply {
                    rowNo = index + 1
                    inventoryCode = order?.inventoryCode
                    invPositionOut = originStock?.locCode
                    invPositionIn = targetLocArchive.locCode
                    batchNo = order?.prodNo
                    rowMemo = prodTag.tagNo
                }
            }

            val u8Request = U8StockMoveRequest().apply {
                orderCode = stockMove.receiptNo
                orderDate = todayStr
                warehouseCode = "01"                  // 固定为 01
                handler = "曾伟生"                    // 固定为 曾伟生
                verifyDate = todayStr
                maker = stockMove.realName ?: stockMove.username
                memo = stockMove.type
                details = detailList
            }

            val response = u8StockMoveService.pushStockMove(u8Request)
            if (response.isSuccess) {
                log.info("U8 移库作业推送成功: receiptNo={}, 明细数量={}", stockMove.receiptNo, detailList.size)
                true
            } else {
                log.error("U8 移库作业推送失败: receiptNo={}, 原因={}", stockMove.receiptNo, response.returnMessage)
                false
            }
        } catch (e: Exception) {
            log.error("U8 移库作业接口调用异常: receiptNo={}", stockMove.receiptNo, e)
            false
        }
    }
}
