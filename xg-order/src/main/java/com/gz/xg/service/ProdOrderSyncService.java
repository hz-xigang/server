package com.gz.xg.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gz.xg.domain.entity.ProdOrder;
import com.gz.xg.domain.enums.SequenceType;
import com.gz.xg.service.plus.ProductionOrderPlusService;
import com.gz.xg.u8.dto.U8PurchaseOrderConvertResult;
import com.gz.xg.u8.dto.U8SalesOrderConvertResult;
import com.gz.xg.u8.service.U8OrderSyncService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 生产订单同步服务
 * 从 U8 系统同步销售订单和采购订单至 WMS 生产订单表
 */
@Service
public class ProdOrderSyncService {

    private final U8OrderSyncService u8OrderSyncService;
    private final ProductionOrderPlusService productionOrderPlusService;
    private final SysSequenceService sysSequenceService;

    public ProdOrderSyncService(
            U8OrderSyncService u8OrderSyncService,
            ProductionOrderPlusService productionOrderPlusService,
            SysSequenceService sysSequenceService
    ) {
        this.u8OrderSyncService = u8OrderSyncService;
        this.productionOrderPlusService = productionOrderPlusService;
        this.sysSequenceService = sysSequenceService;
    }

    /**
     * 同步 U8 销售订单至生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncSalesOrders(String accId) {
        List<U8SalesOrderConvertResult> convertResults = u8OrderSyncService.convertSalesOrders(accId);

        if (convertResults.isEmpty()) {
            return 0;
        }

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> prodNos = sysSequenceService.generateSequences(
                SequenceType.PRODUCTION_ORDER,
                datePrefix,
                convertResults.size()
        );

        List<ProdOrder> prodOrders = new ArrayList<>();
        for (int i = 0; i < convertResults.size(); i++) {
            U8SalesOrderConvertResult result = convertResults.get(i);
            ProdOrder order = new ProdOrder();

            order.setId(IdWorker.getIdStr());
            order.setProdNo(prodNos.get(i));
            order.setErpOrderNo(result.getErpOrderNo());
            order.setType(result.getType());

            order.setCustomerCode(result.getCustomerCode());
            order.setCustomerOrderNo(result.getCustomerOrderNo());
            order.setOrderTypeName(result.getOrderTypeName());
            order.setSaleType(result.getSaleType());
            order.setSalesperson(result.getSalesperson());

            order.setInventoryCode(result.getInventoryCode());
            order.setInventoryName(result.getInventoryName());
            order.setCustMaterialNo(result.getCustMaterialNo());
            order.setM1(result.getM1());
            order.setUnitWeight(result.getUnitWeight());
            order.setMaterial(result.getMaterial());
            order.setBom(result.getBom());
            order.setPackingRequirement(result.getPackingRequirement());
            order.setAnnealingMethod(result.getAnnealingMethod());
            order.setSprayCutting(result.getSprayCutting());
            order.setTechnicalRequirement(result.getTechnicalRequirement());
            order.setPo(result.getPo());
            order.setProcessRoute(result.getProcessRoute());
            order.setSpecWidth(result.getSpecWidth());

            prodOrders.add(order);
        }

        productionOrderPlusService.saveBatch(prodOrders);
        return prodOrders.size();
    }

    /**
     * 同步 U8 采购订单至生产订单表
     *
     * @param accId 账套编号
     * @return 成功同步的订单数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int syncPurchaseOrders(String accId) {
        List<U8PurchaseOrderConvertResult> convertResults = u8OrderSyncService.convertPurchaseOrders(accId);

        if (convertResults.isEmpty()) {
            return 0;
        }

        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> prodNos = sysSequenceService.generateSequences(
                SequenceType.PRODUCTION_ORDER,
                datePrefix,
                convertResults.size()
        );

        List<ProdOrder> prodOrders = new ArrayList<>();
        for (int i = 0; i < convertResults.size(); i++) {
            U8PurchaseOrderConvertResult result = convertResults.get(i);
            ProdOrder order = new ProdOrder();

            order.setId(IdWorker.getIdStr());
            order.setProdNo(prodNos.get(i));
            order.setErpOrderNo(result.getErpOrderNo());
            order.setType(result.getType());

            order.setCustomerCode(result.getCustomerCode());
            order.setCustomerOrderNo(result.getCustomerOrderNo());
            order.setOrderTypeName(result.getOrderTypeName());
            order.setSaleType(result.getSaleType());
            order.setSalesperson(result.getSalesperson());

            order.setInventoryCode(result.getInventoryCode());
            order.setInventoryName(result.getInventoryName());
            order.setCustMaterialNo(result.getCustMaterialNo());
            order.setM1(result.getM1());
            order.setUnitWeight(result.getUnitWeight());
            order.setMaterial(result.getMaterial());
            order.setBom(result.getBom());
            order.setPackingRequirement(result.getPackingRequirement());
            order.setAnnealingMethod(result.getAnnealingMethod());
            order.setSprayCutting(result.getSprayCutting());
            order.setTechnicalRequirement(result.getTechnicalRequirement());
            order.setPo(result.getPo());
            order.setProcessRoute(result.getProcessRoute());
            order.setSpecWidth(result.getSpecWidth());

            prodOrders.add(order);
        }

        productionOrderPlusService.saveBatch(prodOrders);
        return prodOrders.size();
    }
}
