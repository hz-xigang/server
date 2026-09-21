package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.u8.dto.U8OtherStockOutDetailRequest
import com.gz.xg.u8.dto.U8OtherStockOutRequest
import com.gz.xg.u8.service.U8OtherStockOutService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class U8OtherStockOutSyncService(
    private val u8OtherStockOutService: U8OtherStockOutService
) : BaseService() {

    fun syncOtherStockOut(
        tag: VProdTag,
        inventory: StockInventory,
        stockOut: StockOut
    ): U8SyncResult {
        val errorMsg = trySync(tag, inventory, stockOut)
        val status = if (errorMsg == null) 1 else 0
        return U8SyncResult(
            statusMap = mapOf(tag.tagNo to status),
            failCount = if (errorMsg == null) 0 else 1,
            errorMessage = errorMsg
        )
    }

    private fun trySync(tag: VProdTag, inventory: StockInventory, stockOut: StockOut): String? {
        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val qty = inventory.qty ?: tag.qty ?: 0
            val netWeight = inventory.netWeight ?: tag.netWeight ?: BigDecimal.ZERO

            val detail = U8OtherStockOutDetailRequest().apply {
                rowNo = 1
                inventoryCode = tag.inventoryCode ?: ""
                quantity = BigDecimal.valueOf(qty.toLong())
                auxiliaryQuantity = if (netWeight.signum() == 0) BigDecimal.ONE else netWeight
                invPosition = inventory.locCode ?: ""
                batchNo = inventory.u8Batch ?: ""
                rowMemo = tag.tagNo
            }

            val u8Request = U8OtherStockOutRequest().apply {
                orderCode = stockOut.receiptNo
                orderDate = todayStr
                warehouseCode = "01"
                rdCode = "201"
                handler = "曾伟生"
                verifyDate = todayStr
                maker = stockOut.realName ?: stockOut.username
                memo = stockOut.type
                details = listOf(detail)
            }

            val response = u8OtherStockOutService.pushOtherStockOut(u8Request)
            if (response.isSuccess || isDuplicate(response.returnMessage)) {
                log.info("U8 其他出库推送成功: receiptNo={}, tagNo={}", stockOut.receiptNo, tag.tagNo)
                null
            } else {
                log.error("U8 其他出库推送失败: receiptNo={}, tagNo={}, 原因={}", stockOut.receiptNo, tag.tagNo, response.returnMessage)
                response.returnMessage ?: "U8其他出库推送失败"
            }
        } catch (e: Exception) {
            if (isDuplicate(e.message)) {
                log.warn("U8 其他出库单已存在，视为成功: receiptNo={}, tagNo={}", stockOut.receiptNo, tag.tagNo)
                return null
            }
            log.error("U8 其他出库接口调用异常: receiptNo={}, tagNo={}", stockOut.receiptNo, tag.tagNo, e)
            "U8其他出库接口调用异常: ${e.message}"
        }
    }

    private fun isDuplicate(message: String?): Boolean {
        if (message.isNullOrBlank()) return false
        return message.contains("已存在") || message.contains("重复") || message.contains("duplicate", ignoreCase = true)
    }
}
