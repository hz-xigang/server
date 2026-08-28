package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.enums.SequenceType
import com.gz.xg.enums.BusinessType
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.service.U8OrderSyncService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 生产订单同步服务
 * 从 U8 系统同步销售订单和采购订单至 WMS 生产订单表
 */
@Service
class ProdOrderSyncService(
    private val u8OrderSyncService: U8OrderSyncService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService
) {
    private val log = LoggerFactory.getLogger(ProdOrderSyncService::class.java)

    /**
     * 同步 U8 销售订单至生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8销售订单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncSalesOrders(accId: String?): Int {
        log.info("开始查询待同步的 U8 销售订单, 账套: {}", accId ?: "默认")
        val convertResults = u8OrderSyncService.convertSalesOrders(accId)
        if (convertResults.isEmpty()) {
            log.info("U8 销售订单无待同步数据")
            return 0
        }
        log.info("从 U8 转换得到 {} 条销售订单，开始生成生产单号并入库", convertResults.size)

        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prodNos = sysSequenceService.generateSequences(
            SequenceType.PRODUCTION_ORDER,
            datePrefix,
            convertResults.size
        )

        val prodOrders = convertResults.mapIndexed { index, result ->
            ProdOrder().apply {
                id = IdWorker.getIdStr()
                prodNo = prodNos[index]
                erpOrderNo = result.erpOrderNo
                type = result.type

                customerCode = result.customerCode
                customerOrderNo = result.customerOrderNo
                orderTypeName = result.orderTypeName
                saleType = result.saleType
                salesperson = result.salesperson

                inventoryCode = result.inventoryCode
                inventoryName = result.inventoryName
                custMaterialNo = result.custMaterialNo
                m1 = result.m1
                unitWeight = result.unitWeight
                material = result.material
                bom = result.bom
                packingRequirement = result.packingRequirement
                annealingMethod = result.annealingMethod
                sprayCutting = result.sprayCutting
                technicalRequirement = result.technicalRequirement
                po = result.po
                processRoute = result.processRoute
                specWidth = result.specWidth
            }
        }

        productionOrderPlusService.saveBatch(prodOrders)
        log.info("U8 销售订单同步入库完成，生成生产单数量: {}", prodOrders.size)
        return prodOrders.size
    }

    /**
     * 同步 U8 采购订单至生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8采购订单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncPurchaseOrders(accId: String?): Int {
        log.info("开始查询待同步的 U8 采购订单, 账套: {}", accId ?: "默认")
        val convertResults = u8OrderSyncService.convertPurchaseOrders(accId)
        if (convertResults.isEmpty()) {
            log.info("U8 采购订单无待同步数据")
            return 0
        }
        log.info("从 U8 转换得到 {} 条采购订单，开始生成生产单号并入库", convertResults.size)

        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prodNos = sysSequenceService.generateSequences(
            SequenceType.PRODUCTION_ORDER,
            datePrefix,
            convertResults.size
        )

        val prodOrders = convertResults.mapIndexed { index, result ->
            ProdOrder().apply {
                id = IdWorker.getIdStr()
                prodNo = prodNos[index]
                erpOrderNo = result.erpOrderNo
                type = result.type

                customerCode = result.customerCode
                customerOrderNo = result.customerOrderNo
                orderTypeName = result.orderTypeName
                saleType = result.saleType
                salesperson = result.salesperson

                inventoryCode = result.inventoryCode
                inventoryName = result.inventoryName
                custMaterialNo = result.custMaterialNo
                m1 = result.m1
                unitWeight = result.unitWeight
                material = result.material
                bom = result.bom
                packingRequirement = result.packingRequirement
                annealingMethod = result.annealingMethod
                sprayCutting = result.sprayCutting
                technicalRequirement = result.technicalRequirement
                po = result.po
                processRoute = result.processRoute
                specWidth = result.specWidth
                erpOrderId = result.erpOrderId
                qty = result.qty
                inNum = result.inNum
            }
        }

        productionOrderPlusService.saveBatch(prodOrders)
        log.info("U8 采购订单同步入库完成，生成生产单数量: {}", prodOrders.size)
        return prodOrders.size
    }

    /**
     * 同步 U8 生产订单至系统生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8生产订单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncMomOrders(accId: String?): Int {
        log.info("开始查询待同步的 U8 生产订单, 账套: {}", accId ?: "默认")
        val convertResults = u8OrderSyncService.convertMomOrders(accId)
        if (convertResults.isEmpty()) {
            log.info("U8 生产订单无待同步数据")
            return 0
        }
        log.info("从 U8 转换得到 {} 条生产订单，开始生成生产单号并入库", convertResults.size)

        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val prodNos = sysSequenceService.generateSequences(
            SequenceType.PRODUCTION_ORDER,
            datePrefix,
            convertResults.size
        )

        val prodOrders = convertResults.mapIndexed { index, result ->
            ProdOrder().apply {
                id = IdWorker.getIdStr()
                prodNo = prodNos[index]
                erpOrderNo = result.erpOrderNo
                erpOrderId = result.erpOrderId
                type = result.type

                inventoryCode = result.inventoryCode
                inventoryName = result.inventoryName
                spec = result.spec
                specWidth = result.specWidth
                packingRequirement = result.packingRequirement

                qty = result.qty
                inNum = result.inNum

                m1 = result.m1
                m2 = result.m2
                m3 = result.m3
                m4 = result.m4
                m5 = result.m5
            }
        }

        productionOrderPlusService.saveBatch(prodOrders)
        log.info("U8 生产订单同步入库完成，生成生产单数量: {}", prodOrders.size)
        return prodOrders.size
    }
}
