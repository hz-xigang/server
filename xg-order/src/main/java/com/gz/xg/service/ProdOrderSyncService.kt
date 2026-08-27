package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.enums.SequenceType
import com.gz.xg.enums.BusinessType
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.u8.service.U8OrderSyncService
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

    /**
     * 同步 U8 销售订单至生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @OpLog(title = "U8订单同步", opName = "同步U8销售订单", businessType = BusinessType.SYNC)
    @Transactional(rollbackFor = [Exception::class])
    fun syncSalesOrders(accId: String?): Int {
        val convertResults = u8OrderSyncService.convertSalesOrders(accId)
        if (convertResults.isEmpty()) {
            return 0
        }

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
        val convertResults = u8OrderSyncService.convertPurchaseOrders(accId)
        if (convertResults.isEmpty()) {
            return 0
        }

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
        return prodOrders.size
    }
}
