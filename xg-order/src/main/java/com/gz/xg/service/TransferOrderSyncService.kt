package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.enums.BusinessType
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import com.gz.xg.service.plus.TransferOrderPlusService
import com.gz.xg.u8.dto.U8TransferOrderMain
import com.gz.xg.u8.service.U8TransferOrderService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 调拨单同步服务
 * 从 U8 系统同步调拨单至 WMS 调拨单表（TransferOrder 与 TransferOrderDetail）
 */
@Service
class TransferOrderSyncService(
    private val u8TransferOrderService: U8TransferOrderService,
    private val transferOrderPlusService: TransferOrderPlusService,
    private val transferOrderDetailPlusService: TransferOrderDetailPlusService
) {
    private val log = LoggerFactory.getLogger(TransferOrderSyncService::class.java)

    /**
     * 同步 U8 调拨单
     *
     * @param accId 账套编号
     * @return 成功同步的订单主表数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8调拨单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncTransferOrders(accId: String?): Int {
        val response = u8TransferOrderService.queryTransferOrderMain(accId)
        if (!response.isSuccess || response.data == null || response.data.isEmpty()) {
            log.info("U8 调拨单接口返回无数据或未成功: {}", response.returnMessage)
            return 0
        }

        val u8Orders = response.data
        var syncCount = 0

        for (u8Order in u8Orders) {
            val orderCode = u8Order.orderCode
            if (orderCode.isNullOrBlank()) {
                continue
            }

            try {
                syncSingleOrder(u8Order)
                syncCount++
            } catch (e: Exception) {
                log.error("同步调拨单 [{}] 失败: {}", orderCode, e.message, e)
                throw e
            }
        }

        return syncCount
    }

    /**
     * 处理单张调拨单及其明细的保存/更新
     */
    private fun syncSingleOrder(u8Order: U8TransferOrderMain) {
        val orderCode = u8Order.orderCode.trim()

        // 检查库中是否已存在该单据号
        val existingOrder = transferOrderPlusService.getOne(
            LambdaQueryWrapper<TransferOrder>()
                .eq(TransferOrder::getOrderNo, orderCode)
                .last("LIMIT 1")
        )

        val orderId = existingOrder?.id ?: IdWorker.getIdStr()
        val isNew = existingOrder == null

        val order = (existingOrder ?: TransferOrder()).apply {
            if (isNew) {
                id = orderId
                status = 0
                deleted = 0
            }
            orderNo = orderCode
            orderDate = parseDateToLocalDateTime(u8Order.orderDate)
            fromWarehouse = u8Order.fromWarehouseCode
            toWarehouse = u8Order.toWarehouseCode
            fromDept = u8Order.fromDepartmentName ?: u8Order.fromDepartmentCode
            toDept = u8Order.toDepartmentName ?: u8Order.toDepartmentCode
            outCategory = u8Order.outCategoryName
            inCategory = u8Order.inCategoryName
            handler = u8Order.maker
            remark = u8Order.memo
        }

        if (isNew) {
            transferOrderPlusService.save(order)
        } else {
            transferOrderPlusService.updateById(order)
            // 若为更新，先清理历史子表明细
            transferOrderDetailPlusService.remove(
                LambdaQueryWrapper<TransferOrderDetail>().eq(TransferOrderDetail::getPId, orderId)
            )
        }

        // 保存子表明细
        val u8Details = u8Order.details
        if (!u8Details.isNullOrEmpty()) {
            val detailEntities = u8Details.map { d ->
                TransferOrderDetail().apply {
                    id = IdWorker.getIdStr()
                    pId = orderId
                    inventoryCode = d.inventoryCode
                    inventoryName = d.inventoryName
                    spec = d.spec
                    unit = d.unitName
                    qty = d.quantity
                    batchNo = d.batchNo
                    specWidth = d.specWidth
                    customerCode = d.customerCode
                    packingMethod = d.packingMethod
                    m1 = d.auxiliaryQuantity?.stripTrailingZeros()?.toPlainString() // 辅计量数量
                    m2 = d.invPosition // 货位
                    m3 = d.rowMemo // 行备注
                }
            }
            transferOrderDetailPlusService.saveBatch(detailEntities)
        }
    }

    /**
     * 将用友日期字符串转为 LocalDateTime 并补齐时分秒
     */
    private fun parseDateToLocalDateTime(dateStr: String?): LocalDateTime? {
        if (dateStr.isNullOrBlank()) {
            return null
        }
        val trimmed = dateStr.trim()
        return try {
            if (trimmed.length == 10) {
                LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay()
            } else {
                LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
        } catch (e: Exception) {
            log.warn("解析单据日期失败: {}", dateStr)
            null
        }
    }
}
