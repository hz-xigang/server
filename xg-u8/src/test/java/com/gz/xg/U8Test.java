package com.gz.xg;

import com.gz.xg.u8.dto.U8PurchaseOrderMain;
import com.gz.xg.u8.dto.U8Response;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用友接口测试
 */
@Slf4j
@SpringBootTest
public class U8Test {

    @Autowired
    private U8PurchaseOrderService u8PurchaseOrderService;

    @Test
    public void testQueryPurchaseOrderMain() {
        log.info("开始测试查询采购订单主表接口");

        U8Response<U8PurchaseOrderMain> response = u8PurchaseOrderService.queryPurchaseOrderMain(null);

        log.info("接口调用完成");
        log.info("返回码: {}", response.getCode());
        log.info("返回信息: {}", response.getReturnMessage());

        if (response.isSuccess() && response.getData() != null) {
            log.info("查询到 {} 条采购订单", response.getData().size());

            response.getData().forEach(order -> {
                log.info("订单号: {}, 供应商: {}, 单据日期: {}, 明细数: {}",
                        order.getCcode(),
                        order.getCvenname(),
                        order.getDdate(),
                        order.getDetails() != null ? order.getDetails().size() : 0);

                if (order.getDetails() != null) {
                    order.getDetails().forEach(detail -> {
                        log.info("  - 存货: {}, 数量: {}, 到货日期: {}",
                                detail.getCinvname(),
                                detail.getIquantity(),
                                detail.getDarrivedate());
                    });
                }
            });
        } else {
            log.error("接口调用失败: {}", response.getReturnMessage());
        }
    }
}
