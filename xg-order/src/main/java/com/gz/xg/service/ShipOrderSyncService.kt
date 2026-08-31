package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.enums.BusinessType
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import com.gz.xg.service.plus.ShipOrderPlusService
import com.gz.xg.u8.dto.U8DispatchListMain
import com.gz.xg.u8.service.U8DispatchListService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 发货单同步服务
 * 从 U8 系统同步发货单至 WMS 发货指令单表（ShipOrder 与 ShipOrderDetail）
 */
@Service
class ShipOrderSyncService(
    private val u8DispatchListService: U8DispatchListService,
    private val shipOrderPlusService: ShipOrderPlusService,
    private val shipOrderDetailPlusService: ShipOrderDetailPlusService
) {
    private val log = LoggerFactory.getLogger(ShipOrderSyncService::class.java)

    /**
     * 同步 U8 发货单
     *
     * @param accId 账套编号
     * @return 成功同步的发货单主表数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8发货单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncShipOrders(accId: String?): Int {
        log.info("开始查询待同步的 U8 发货单, 账套: {}", accId ?: "默认")
        val response = u8DispatchListService.queryDispatchList(accId)
        if (!response.isSuccess || response.data == null || response.data.isEmpty()) {
            log.info("U8 发货单接口返回无数据或未成功: {}", response.returnMessage)
            return 0
        }

        val u8Orders = response.data
        log.info("从 U8 获取到 {} 条发货单，开始逐条同步入库", u8Orders.size)
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
                log.error("同步发货单 [{}] 失败: {}", orderCode, e.message, e)
                throw e
            }
        }

        log.info("U8 发货单同步完成，成功同步 {}/{} 条", syncCount, u8Orders.size)
        return syncCount
    }

    /**
     * 处理单张发货单及其明细的保存/更新
     */
    private fun syncSingleOrder(u8Order: U8DispatchListMain) {
        val orderCode = u8Order.orderCode.trim()

        // 检查库中是否已存在该 ERP 发货单号 (erpOrderNo) 或 shipNo
        val existingOrder = shipOrderPlusService.getOne(
            QueryWrapper<ShipOrder>()
                .select("top 1 *")
                .eq("erpOrderNo", orderCode)
                .or()
                .eq("shipNo", orderCode)
        )

        val orderId = existingOrder?.id ?: IdWorker.getIdStr()
        val isNew = existingOrder == null

        val order = (existingOrder ?: ShipOrder()).apply {
            if (isNew) {
                id = orderId
                shipNo = orderCode
                status = 0
                deleted = 0
                createTime = parseDateToLocalDateTime(u8Order.orderDate) ?: LocalDateTime.now()
            }
            erpOrderNo = orderCode
            salesType = u8Order.salesTypeName ?: u8Order.salesTypeCode
            salesDept = u8Order.departmentName ?: u8Order.departmentCode
            customerCode = u8Order.customerCode
            salesman = u8Order.personName ?: u8Order.personCode
            isTax = parseIsTax(u8Order.define3)
            m1 = u8Order.define10 // 客户订单号/PO单号
            m2 = u8Order.customerName // 客户名称
            m3 = u8Order.memo // 单据备注
            m4 = u8Order.maker // 制单人
            m5 = u8Order.verifier // 审核人
        }

        if (isNew) {
            shipOrderPlusService.save(order)
        } else {
            shipOrderPlusService.updateById(order)
            // 若为更新，先清理历史子表明细
            shipOrderDetailPlusService.remove(
                LambdaQueryWrapper<ShipOrderDetail>().eq(ShipOrderDetail::getPId, orderId)
            )
        }

        // 保存子表明细
        val u8Details = u8Order.details
        if (!u8Details.isNullOrEmpty()) {
            val now = LocalDateTime.now()
            val detailEntities = u8Details.map { d ->
                ShipOrderDetail().apply {
                    id = IdWorker.getIdStr()
                    pId = orderId
                    orderNo = orderCode
                    inventoryCode = d.inventoryCode
                    inventoryName = d.inventoryName
                    spec = d.spec
                    material = d.material
                    unit = d.unitName
                    qty = d.quantity
                    weight = d.auxiliaryQuantity
                    unitWeight = d.unitWeight
                    packingMethod = d.packingMethod
                    specWidth = d.specWidth
                    customerCode = d.customerCode ?: u8Order.customerCode
                    createTime = now
                    deleted = 0
                    m1 = d.technicalRequirement // 技术要求
                    m2 = d.annealingMethod // 退火方式
                    m3 = d.sprayCutting // 喷涂线切割
                    m4 = d.rowMemo // 行备注
                    m5 = d.salesOrderDetailId?.toString() // 销售订单行主键 (isosid)
                }
            }
            shipOrderDetailPlusService.saveBatch(detailEntities)
        }
    }

    /**
     * 判断是否含税 ("是" -> true, 其它 -> false)
     */
    private fun parseIsTax(taxDefine: String?): Boolean {
        if (taxDefine.isNullOrBlank()) return false
        val trimmed = taxDefine.trim()
        return "是" == trimmed || "1" == trimmed || "true".equals(trimmed, ignoreCase = true)
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
