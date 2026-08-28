package com.gz.xg.schedule

import com.gz.xg.service.ProdOrderSyncService
import com.gz.xg.service.TransferOrderSyncService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * U8 订单同步定时任务
 * 定时自动从 U8 系统同步各类单据至 WMS
 */
@Component
class U8OrderSyncScheduler(
    private val prodOrderSyncService: ProdOrderSyncService,
    private val transferOrderSyncService: TransferOrderSyncService
) {
    private val log = LoggerFactory.getLogger(U8OrderSyncScheduler::class.java)

    /**
     * 定时同步销售订单和采购订单
     * 每小时的 00 和 30 分执行（异步）
     */
    @Scheduled(cron = "0 0,6 * * * ?")
     fun syncU8Orders() {
        log.info("开始执行 U8 订单同步任务")

        // 异步同步销售订单
        //syncSalesOrdersAsync()

        // 异步同步采购订单
        syncPurchaseOrdersAsync()

        log.info("U8 订单同步任务已提交（异步执行）")
    }

    /**
     * 定时同步调拨单
     * 每小时的 00, 20, 40 分执行（异步）
     */
    //@Scheduled(cron = "0 0,20,40 * * * ?")
    open fun syncU8TransferOrders() {
        log.info("开始执行 U8 调拨单同步任务")
        syncTransferOrdersAsync()
        log.info("U8 调拨单同步任务已提交（异步执行）")
    }

    /**
     * 定时同步生产订单
     * 每小时的 00, 20, 40 分执行（异步）
     */
    @Scheduled(cron = "0 0,6,40 * * * ?")
     fun syncU8MomOrders() {
        log.info("开始执行 U8 生产订单同步任务")
        syncMomOrdersAsync()
        log.info("U8 生产订单同步任务已提交（异步执行）")
    }

    /**
     * 异步同步销售订单
     */
    @Async
     fun syncSalesOrdersAsync() {
        try {
            log.info("开始同步 U8 销售订单")
            val count = prodOrderSyncService.syncSalesOrders(null)
            log.info("U8 销售订单同步完成，共同步 {} 条", count)
        } catch (e: Exception) {
            log.error("U8 销售订单同步失败", e)
        }
    }

    /**
     * 异步同步采购订单
     */
    @Async
     fun syncPurchaseOrdersAsync() {
        try {
            log.info("开始同步 U8 采购订单")
            val count = prodOrderSyncService.syncPurchaseOrders(null)
            log.info("U8 采购订单同步完成，共同步 {} 条", count)
        } catch (e: Exception) {
            log.error("U8 采购订单同步失败", e)
        }
    }

    /**
     * 异步同步调拨单
     */
    @Async
     fun syncTransferOrdersAsync() {
        try {
            log.info("开始同步 U8 调拨单")
            val count = transferOrderSyncService.syncTransferOrders(null)
            log.info("U8 调拨单同步完成，共同步 {} 条", count)
        } catch (e: Exception) {
            log.error("U8 调拨单同步失败", e)
        }
    }

    /**
     * 异步同步生产订单
     */
    @Async
    fun syncMomOrdersAsync() {
        try {
            log.info("开始同步 U8 生产订单")
            val count = prodOrderSyncService.syncMomOrders(null)
            log.info("U8 生产订单同步完成，共同步 {} 条", count)
        } catch (e: Exception) {
            log.error("U8 生产订单同步失败", e)
        }
    }
}
