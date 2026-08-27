package com.gz.xg.u8.service

import com.gz.xg.u8.dto.U8PurchaseOrderConvertResult
import com.gz.xg.u8.dto.U8SalesOrderConvertResult
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * U8 订单同步服务
 * 将用友 U8 销售订单和采购订单同步至 WMS 生产订单表
 */
@Service
class U8OrderSyncService(
    private val u8SalesOrderService: U8SalesOrderService,
    private val u8PurchaseOrderService: U8PurchaseOrderService
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
