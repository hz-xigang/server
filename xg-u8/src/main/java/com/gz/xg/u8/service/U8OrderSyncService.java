package com.gz.xg.u8.service;

import com.gz.xg.u8.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * U8 订单同步服务
 * 将用友 U8 销售订单和采购订单同步至 WMS 生产订单表
 */
@Service
public class U8OrderSyncService {

    private final U8SalesOrderService u8SalesOrderService;
    private final U8PurchaseOrderService u8PurchaseOrderService;

    public U8OrderSyncService(
            U8SalesOrderService u8SalesOrderService,
            U8PurchaseOrderService u8PurchaseOrderService
    ) {
        this.u8SalesOrderService = u8SalesOrderService;
        this.u8PurchaseOrderService = u8PurchaseOrderService;
    }

    /**
     * 转换销售订单为生产订单数据传输对象
     *
     * @param accId 账套编号
     * @return 生产订单DTO列表
     */
    public List<U8SalesOrderConvertResult> convertSalesOrders(String accId) {
        U8Response<U8SalesOrderMain> response = u8SalesOrderService.querySalesOrderMain(accId);

        if (!response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
            return new ArrayList<>();
        }

        List<U8SalesOrderMain> salesOrders = response.getData();
        List<U8SalesOrderConvertResult> results = new ArrayList<>();

        for (U8SalesOrderMain main : salesOrders) {
            for (U8SalesOrderDetail detail : main.getDetails()) {
                U8SalesOrderConvertResult result = new U8SalesOrderConvertResult();
                result.setErpOrderNo(main.getOrderCode());
                result.setType(0); // 0-销售订单

                result.setCustomerCode(main.getCustomerCode());
                result.setCustomerOrderNo(main.getCustomerOrderNo());
                result.setOrderTypeName(main.getOrderTypeName());
                result.setSaleType(main.getSalesTypeName());
                result.setSalesperson(main.getSalesperson());

                result.setInventoryCode(detail.getInventoryCode());
                result.setInventoryName(detail.getInventoryName());
                result.setCustMaterialNo(detail.getCustomerMaterialNo());
                result.setM1(detail.getRowMemo());
                result.setUnitWeight(toBigDecimal(detail.getUnitWeight()));
                result.setMaterial(detail.getMaterial());
                result.setBom(detail.getBom());
                result.setPackingRequirement(detail.getPackingRequirement());
                result.setAnnealingMethod(detail.getAnnealingMethod());
                result.setSprayCutting(detail.getSprayCutting());
                result.setTechnicalRequirement(detail.getTechnicalRequirement());
                result.setPo(detail.getPo());
                result.setProcessRoute(detail.getProcessRoute());
                result.setSpecWidth(toBigDecimal(detail.getSpecWidth()));

                results.add(result);
            }
        }

        return results;
    }

    /**
     * 转换采购订单为生产订单数据传输对象
     *
     * @param accId 账套编号
     * @return 生产订单DTO列表
     */
    public List<U8PurchaseOrderConvertResult> convertPurchaseOrders(String accId) {
        U8Response<U8PurchaseOrderMain> response = u8PurchaseOrderService.queryPurchaseOrderMain(accId);

        if (!response.isSuccess() || response.getData() == null || response.getData().isEmpty()) {
            return new ArrayList<>();
        }

        List<U8PurchaseOrderMain> purchaseOrders = response.getData();
        List<U8PurchaseOrderConvertResult> results = new ArrayList<>();

        for (U8PurchaseOrderMain main : purchaseOrders) {
            for (U8PurchaseOrderDetail detail : main.getDetails()) {
                U8PurchaseOrderConvertResult result = new U8PurchaseOrderConvertResult();
                result.setErpOrderNo(main.getOrderCode());
                result.setType(1); // 1-采购订单

                result.setCustomerCode(main.getVendorCode());
                result.setCustomerOrderNo(main.getCustomerOrderNo());
                result.setOrderTypeName(main.getOrderTypeName());
                result.setSaleType(main.getPurchaseTypeName());
                result.setSalesperson(main.getSalesperson());

                result.setInventoryCode(detail.getInventoryCode());
                result.setInventoryName(detail.getInventoryName());
                result.setCustMaterialNo(detail.getCustomerMaterialNo());
                result.setM1(detail.getRowMemo());
                result.setUnitWeight(toBigDecimal(detail.getUnitWeight()));
                result.setMaterial(detail.getMaterial());
                result.setBom(detail.getBom());
                result.setPackingRequirement(detail.getPackingRequirement());
                result.setAnnealingMethod(detail.getAnnealingMethod());
                result.setSprayCutting(detail.getSprayCutting());
                result.setTechnicalRequirement(detail.getTechnicalRequirement());
                result.setPo(detail.getPo());
                result.setProcessRoute(detail.getProcessRoute());
                result.setSpecWidth(toBigDecimal(detail.getSpecWidth()));

                results.add(result);
            }
        }

        return results;
    }

    private static BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }
}
