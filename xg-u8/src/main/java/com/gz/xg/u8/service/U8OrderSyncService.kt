package com.gz.xg.u8.service

import com.gz.xg.u8.dto.U8MomOrderConvertResult
import com.gz.xg.u8.dto.U8PurchaseOrderConvertResult
import com.gz.xg.u8.dto.U8SalesOrderConvertResult
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * U8 订单同步服务
 * 将用友 U8 销售订单、采购订单和生产订单同步至 WMS 生产订单表
 */
@Service
class U8OrderSyncService(
    private val u8SalesOrderService: U8SalesOrderService,
    private val u8PurchaseOrderService: U8PurchaseOrderService,
    private val u8MomOrderService: U8MomOrderService
) {

    /**
     * 转换销售订单为生产订单数据传输对象
     *
     * @param accId 账套编号
     * @return 生产订单DTO列表
     */
     fun convertSalesOrders(accId: String?): List<U8SalesOrderConvertResult> {
        val response = u8SalesOrderService.querySalesOrderMain(accId)

        if (!response.isSuccess || response.data == null || response.data.isEmpty()) {
            return emptyList()
        }

        val salesOrders = response.data
        val results = mutableListOf<U8SalesOrderConvertResult>()

        for (main in salesOrders) {
            val details = main.details ?: continue
            for (detail in details) {
                val result = U8SalesOrderConvertResult().apply {
                    erpOrderNo = main.orderCode
                    type = 0 // 0-销售订单

                    customerCode = main.customerCode
                    customerOrderNo = main.customerOrderNo
                    orderTypeName = main.orderTypeName
                    saleType = main.salesTypeName
                    salesperson = main.salesperson

                    inventoryCode = detail.inventoryCode
                    inventoryName = detail.inventoryName
                    custMaterialNo = detail.customerMaterialNo
                    m1 = detail.rowMemo
                    unitWeight = toBigDecimal(detail.unitWeight)
                    material = detail.material
                    bom = detail.bom
                    packingRequirement = detail.packingRequirement
                    annealingMethod = detail.annealingMethod
                    sprayCutting = detail.sprayCutting
                    technicalRequirement = detail.technicalRequirement
                    po = detail.po
                    processRoute = detail.processRoute
                    specWidth = toBigDecimal(detail.specWidth)
                    erpOrderNo = main.salesOrderId.toString()
                }

                results.add(result)
            }
        }

        return results
    }

    /**
     * 转换采购订单为生产订单数据传输对象
     *
     * @param accId 账套编号
     * @return 生产订单DTO列表
     */
     fun convertPurchaseOrders(accId: String?): List<U8PurchaseOrderConvertResult> {
        val response = u8PurchaseOrderService.queryPurchaseOrderMain(accId)

        if (!response.isSuccess || response.data == null || response.data.isEmpty()) {
            return emptyList()
        }

        val purchaseOrders = response.data
        val results = mutableListOf<U8PurchaseOrderConvertResult>()

        for (main in purchaseOrders) {
            val details = main.details ?: continue
            for (detail in details) {
                val result = U8PurchaseOrderConvertResult().apply {
                    erpOrderNo = main.orderCode
                    type = 1 // 1-采购订单

                    customerCode = main.vendorCode
                    customerOrderNo = main.customerOrderNo
                    orderTypeName = main.orderTypeName
                    saleType = main.purchaseTypeName
                    salesperson = main.salesperson

                    inventoryCode = detail.inventoryCode
                    inventoryName = detail.inventoryName
                    custMaterialNo = detail.customerMaterialNo
                    m1 = detail.rowMemo
                    unitWeight = toBigDecimal(detail.unitWeight)
                    material = detail.material
                    bom = detail.bom
                    packingRequirement = detail.packingRequirement
                    annealingMethod = detail.annealingMethod
                    sprayCutting = detail.sprayCutting
                    technicalRequirement = detail.technicalRequirement
                    po = detail.po
                    processRoute = detail.processRoute
                    specWidth = toBigDecimal(detail.specWidth)
                    erpOrderId = main.purchaseOrderId.toString()
                }

                results.add(result)
            }
        }

        return results
    }

    /**
     * 转换生产订单为系统生产订单数据传输对象
     *
     * @param accId 账套编号
     * @return 生产订单DTO列表
     */
    fun convertMomOrders(accId: String?): List<U8MomOrderConvertResult> {
        val response = u8MomOrderService.queryMomOrderMain(accId)

        if (!response.isSuccess || response.data == null || response.data.isEmpty()) {
            return emptyList()
        }

        val momOrders = response.data
        val results = mutableListOf<U8MomOrderConvertResult>()

        for (main in momOrders) {
            val details = main.details ?: continue
            for (detail in details) {
                val result = U8MomOrderConvertResult().apply {
                    erpOrderNo = main.orderCode
                    erpOrderId = detail.detailId?.toString()
                    type = 2 // 2-生产订单

                    inventoryCode = detail.inventoryCode
                    inventoryName = detail.inventoryName
                    spec = detail.spec
                    specWidth = toBigDecimal(detail.specWidth)
                    packingRequirement = detail.packingMethod

                    qty = detail.quantity
                    inNum = detail.auxiliaryQuantity

                    m1 = detail.departmentName ?: detail.departmentCode
                    m2 = detail.warehouseName ?: detail.warehouseCode
                    m3 = detail.rowMemo
                    m4 = main.memo
                }

                results.add(result)
            }
        }

        return results
    }

    private fun toBigDecimal(value: String?): BigDecimal? {
        if (value.isNullOrBlank()) {
            return null
        }
        return BigDecimal(value.trim())
    }
}
