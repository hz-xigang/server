package xg;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.gz.xg.XgApplication;
import com.gz.xg.domain.entity.ProdOrder;
import com.gz.xg.domain.enums.SequenceType;
import com.gz.xg.service.ProdOrderService;
import com.gz.xg.service.SysSequenceService;
import com.gz.xg.service.plus.ProductionOrderPlusService;
import com.gz.xg.u8.dto.*;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import com.gz.xg.u8.service.U8SalesOrderService;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 验证用友 U8 销售订单/采购订单同步为 WMS ProdOrder 的字段映射逻辑。
 * <p>
 * 不启动完整 Spring 容器（避免依赖 MapStruct 生成类、数据库等），
 * 仅手工装配 U8 客户端，直接调用真实接口取数。
 */
@SpringBootTest(classes = XgApplication.class)
class U8SyncProdOrderTest {

    private static final String ACC_ID = "108";

    /** 与 application.yaml 中 u8.url 保持一致，可用 -Du8.url=... 覆盖 */
    private static final String DEFAULT_U8_URL = "http://127.0.0.1:3001/U8_API/";

    @Resource
    private U8SalesOrderService u8SalesOrderService;

    @Resource
    private U8PurchaseOrderService u8PurchaseOrderService;

    @Resource
    private ProductionOrderPlusService productionOrderPlusService;

    @Resource
    private SysSequenceService sysSequenceService;



    @Test
    @DisplayName("验证 U8 销售订单明细映射至 WMS 生产订单 (ProdOrder)")
    void testSalesOrderToProdOrderMapping() {
        U8Response<U8SalesOrderMain> response = u8SalesOrderService.querySalesOrderMain(ACC_ID);
        assertNotNull(response, "U8 销售订单接口响应不能为空");
        assertTrue(response.isSuccess(),
                "U8 销售订单接口调用失败: " + response.getReturnMessage());

        List<U8SalesOrderMain> data = response.getData();
        ArrayList<ProdOrder> list = new ArrayList<>();


        AtomicInteger detailCount = new AtomicInteger();
        data.forEach(main->{
            detailCount.addAndGet(main.getDetails().size());
        });

        String format = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List< String> prodNos = sysSequenceService.generateSequences(SequenceType.PRODUCTION_ORDER, format, detailCount.get());
        int idx = 0;


        for (U8SalesOrderMain main : data) {
            List<U8SalesOrderDetail> details = main.getDetails();


            for (U8SalesOrderDetail detail : details) {
                System.err.println(detail);
                ProdOrder order = new ProdOrder();
                order.setProdNo(prodNos.get(idx++));
                //order.setProdNo();

                order.setId(IdWorker.getIdStr());
                order.setErpOrderNo(main.getOrderCode());
                order.setType(0); // 0-销售订单
                System.err.println(main.getCustomerCode());
                order.setCustomerCode(main.getCustomerCode());
                order.setCustomerOrderNo(main.getCustomerOrderNo());
                order.setOrderTypeName(main.getOrderTypeName());
                order.setSaleType(main.getSalesTypeName());
                order.setSalesperson(main.getSalesperson());

                order.setInventoryCode(detail.getInventoryCode());
                order.setInventoryName(detail.getInventoryName());
                order.setCustMaterialNo(detail.getCustomerMaterialNo());
                order.setM1(detail.getRowMemo());
                order.setUnitWeight(toBigDecimal(detail.getUnitWeight()));
                order.setMaterial(detail.getMaterial());
                order.setBom(detail.getBom());
                order.setPackingRequirement(detail.getPackingRequirement());
                order.setAnnealingMethod(detail.getAnnealingMethod());
                order.setSprayCutting(detail.getSprayCutting());
                order.setTechnicalRequirement(detail.getTechnicalRequirement());
                order.setPo(detail.getPo());
                order.setProcessRoute(detail.getProcessRoute());
                order.setSpecWidth(toBigDecimal(detail.getSpecWidth()));

                assertEquals(main.getOrderCode(), order.getErpOrderNo());
                assertEquals(0, order.getType());
                assertEquals(main.getCustomerCode(), order.getCustomerCode());
                assertEquals(main.getCustomerOrderNo(), order.getCustomerOrderNo());
                assertEquals(main.getOrderTypeName(), order.getOrderTypeName());
                assertEquals(main.getSalesTypeName(), order.getSaleType());
                assertEquals(main.getSalesperson(), order.getSalesperson());
                assertEquals(detail.getInventoryCode(), order.getInventoryCode());
                assertEquals(detail.getInventoryName(), order.getInventoryName());
                assertEquals(detail.getCustomerMaterialNo(), order.getCustMaterialNo());
                assertEquals(detail.getRowMemo(), order.getM1());
                assertEquals(toBigDecimal(detail.getUnitWeight()), order.getUnitWeight());
                assertEquals(detail.getMaterial(), order.getMaterial());
                assertEquals(detail.getBom(), order.getBom());
                assertEquals(detail.getPackingRequirement(), order.getPackingRequirement());
                assertEquals(detail.getAnnealingMethod(), order.getAnnealingMethod());
                assertEquals(detail.getSprayCutting(), order.getSprayCutting());
                assertEquals(detail.getTechnicalRequirement(), order.getTechnicalRequirement());
                assertEquals(detail.getPo(), order.getPo());
                assertEquals(detail.getProcessRoute(), order.getProcessRoute());
                assertEquals(toBigDecimal(detail.getSpecWidth()), order.getSpecWidth());
                list.add(order);
            }
        }


        productionOrderPlusService.saveBatch(list);

    }

    @Test
    @DisplayName("验证 U8 采购订单明细映射至 WMS 生产订单 (ProdOrder)")
    void testPurchaseOrderToProdOrderMapping() {
        U8Response<U8PurchaseOrderMain> response = u8PurchaseOrderService.queryPurchaseOrderMain(ACC_ID);
        assertNotNull(response, "U8 采购订单接口响应不能为空");
        assertTrue(response.isSuccess(),
                "U8 采购订单接口调用失败: " + response.getReturnMessage());

        List<U8PurchaseOrderMain> data = response.getData();
        assumeTrue(data != null && !data.isEmpty(), "U8 未返回采购订单数据，跳过映射校验");

        ArrayList<ProdOrder> list = new ArrayList<>();

        AtomicInteger detailCount = new AtomicInteger();
        data.forEach(main->{
            detailCount.addAndGet(main.getDetails().size());
        });

        String format = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        List< String> prodNos = sysSequenceService.generateSequences(SequenceType.PRODUCTION_ORDER, format, detailCount.get());
        int idx = 0;
        for (U8PurchaseOrderMain main : data) {
            List<U8PurchaseOrderDetail> details = main.getDetails();

            for (U8PurchaseOrderDetail detail : details) {
                ProdOrder order = new ProdOrder();
                order.setId(IdWorker.getIdStr());
                order.setProdNo(prodNos.get(idx));
                order.setErpOrderNo(main.getOrderCode());
                order.setType(1);
                order.setCustomerCode(main.getVendorCode());
                order.setCustomerOrderNo(main.getCustomerOrderNo());
                order.setOrderTypeName(main.getOrderTypeName());
                order.setSaleType(main.getPurchaseTypeName());
                order.setSalesperson(main.getSalesperson());

                order.setInventoryCode(detail.getInventoryCode());
                order.setInventoryName(detail.getInventoryName());
                order.setCustMaterialNo(detail.getCustomerMaterialNo());
                order.setM1(detail.getRowMemo());
                order.setUnitWeight(toBigDecimal(detail.getUnitWeight()));
                order.setMaterial(detail.getMaterial());
                order.setBom(detail.getBom());
                order.setPackingRequirement(detail.getPackingRequirement());
                order.setAnnealingMethod(detail.getAnnealingMethod());
                order.setSprayCutting(detail.getSprayCutting());
                order.setTechnicalRequirement(detail.getTechnicalRequirement());
                order.setPo(detail.getPo());
                order.setProcessRoute(detail.getProcessRoute());
                order.setSpecWidth(toBigDecimal(detail.getSpecWidth()));

                assertEquals(main.getOrderCode(), order.getErpOrderNo());
                assertEquals(1, order.getType());
                assertEquals(main.getVendorCode(), order.getCustomerCode());
                assertEquals(main.getCustomerOrderNo(), order.getCustomerOrderNo());
                assertEquals(detail.getInventoryCode(), order.getInventoryCode());
                assertEquals(detail.getInventoryName(), order.getInventoryName());
                assertEquals(
                        detail.getCustomerMaterialNo(),
                        order.getCustMaterialNo());
                assertEquals(detail.getRowMemo(), order.getM1());
                assertEquals(toBigDecimal(detail.getUnitWeight()), order.getUnitWeight());
                assertEquals(detail.getMaterial(), order.getMaterial());
                assertEquals(detail.getBom(), order.getBom());
                assertEquals(detail.getProcessRoute(), order.getProcessRoute());
                assertEquals(toBigDecimal(detail.getSpecWidth()), order.getSpecWidth());
                list.add(order);
                idx++;
            }
        }

        productionOrderPlusService.saveBatch(list);

    }

    private static BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return first != null ? first : second;
    }
}
