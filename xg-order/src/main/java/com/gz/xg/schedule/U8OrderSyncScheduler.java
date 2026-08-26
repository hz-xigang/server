package com.gz.xg.schedule;

import com.gz.xg.service.ProdOrderSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * U8 订单同步定时任务
 * 每小时的 00 和 30 分自动从 U8 系统同步订单至 WMS
 */
@Slf4j
@Component
public class U8OrderSyncScheduler {

    private final ProdOrderSyncService prodOrderSyncService;

    public U8OrderSyncScheduler(ProdOrderSyncService prodOrderSyncService) {
        this.prodOrderSyncService = prodOrderSyncService;
    }

    /**
     * 定时同步销售订单和采购订单
     * 每小时的 00 和 30 分执行（异步）
     */
    @Scheduled(cron = "0 0,30 * * * ?")
    public void syncU8Orders() {
        log.info("开始执行 U8 订单同步任务");

        // 异步同步销售订单
        syncSalesOrdersAsync();

        // 异步同步采购订单
        syncPurchaseOrdersAsync();

        log.info("U8 订单同步任务已提交（异步执行）");
    }

    /**
     * 异步同步销售订单
     */
    @Async
    public void syncSalesOrdersAsync() {
        try {
            log.info("开始同步 U8 销售订单");
            int count = prodOrderSyncService.syncSalesOrders(null);
            log.info("U8 销售订单同步完成，共同步 {} 条", count);
        } catch (Exception e) {
            log.error("U8 销售订单同步失败", e);
        }
    }

    /**
     * 异步同步采购订单
     */
    @Async
    public void syncPurchaseOrdersAsync() {
        try {
            log.info("开始同步 U8 采购订单");
            int count = prodOrderSyncService.syncPurchaseOrders(null);
            log.info("U8 采购订单同步完成，共同步 {} 条", count);
        } catch (Exception e) {
            log.error("U8 采购订单同步失败", e);
        }
    }
}
