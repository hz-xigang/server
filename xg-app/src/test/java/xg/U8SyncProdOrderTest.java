package xg;

import com.gz.xg.domain.entity.ProdOrder;
import com.gz.xg.u8.dto.U8PurchaseOrderDetail;
import com.gz.xg.u8.dto.U8PurchaseOrderMain;
import com.gz.xg.u8.dto.U8SalesOrderDetail;
import com.gz.xg.u8.dto.U8SalesOrderMain;
import com.gz.xg.u8.service.U8PurchaseOrderService;
import com.gz.xg.u8.service.U8SalesOrderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证用友 U8 销售订单/采购订单同步为 WMS ProdOrder 的字段映射逻辑
 */
class U8SyncProdOrderTest {

    @Resource
    private U8SalesOrderService u8SalesOrderService;

    @Resource
    private U8PurchaseOrderService u8PurchaseOrderService;


    @Test
    @DisplayName("验证 U8 销售订单明细映射至 WMS 生产订单 (ProdOrder)")
    void testSalesOrderToProdOrderMapping() {
        

        // 1. 模拟构建 U8SalesOrderMain 与 U8SalesOrderDetail
        U8SalesOrderMain main = new U8SalesOrderMain();
        main.setCustomerName("测试客户A");
        main.setCustomerOrderNo("PO-CUST-888");
        main.setDepartmentName("销售一部");

        U8SalesOrderDetail detail = new U8SalesOrderDetail();
        detail.setDetailId(10001L);
        detail.setInventoryCode("INV-001");
        detail.setInventoryName("硅钢片A");
        detail.setCustomerMaterialNo("CUST-MAT-999");
        detail.setAuxiliaryQuantity(new BigDecimal("10.00"));
        detail.setQuantity(new BigDecimal("100.00"));
        detail.setRowMemo("加急生产");
        detail.setDeliveryDate("2026-09-01");
        detail.setPlannedCompletionDate("2026-08-30");
        detail.setSpecWidth("0.35");
        detail.setUnitWeight("2.5");
        detail.setMaterial("35WW300");
        detail.setBom("BOM-V1");
        detail.setPackingRequirement("防潮木箱");
        detail.setAnnealingMethod("充氮退火");
        detail.setSprayCutting("自动喷涂");
        detail.setTechnicalRequirement("表面无划痕");
        detail.setPo("PO-2026-X1");
        detail.setProcessRoute("冲压->退火->包装");

        List<U8SalesOrderDetail> details = new ArrayList<>();
        details.add(detail);
        main.setDetails(details);

        // 2. 执行映射转换逻辑（模拟业务服务层的同步落库映射）
        ProdOrder order = new ProdOrder();
        // 主表字段映射
        order.setErpOrderNo(main.getOrderCode());
        order.setType(0); // 0-销售订单
        order.setCustomerCode(main.getCustomerCode());
        order.setCustomerOrderNo(main.getCustomerOrderNo());
        order.setOrderTypeName(main.getOrderTypeName());
        order.setSaleType(main.getSalesTypeName());
        order.setSalesperson(main.getSalesperson());

        // 明细字段映射
        order.setInventoryCode(detail.getInventoryCode());
        order.setInventoryName(detail.getInventoryName());
        order.setCustMaterialNo(detail.getCustomerMaterialNo());
        order.setM1(detail.getRowMemo());

        if (detail.getUnitWeight() != null) {
            order.setUnitWeight(new BigDecimal(detail.getUnitWeight()));
        }
        order.setMaterial(detail.getMaterial());
        order.setBom(detail.getBom());
        order.setPackingRequirement(detail.getPackingRequirement());
        order.setAnnealingMethod(detail.getAnnealingMethod());
        order.setSprayCutting(detail.getSprayCutting());
        order.setTechnicalRequirement(detail.getTechnicalRequirement());
        order.setPo(detail.getPo());
        order.setProcessRoute(detail.getProcessRoute());

        if (detail.getSpecWidth() != null) {
            order.setSpecWidth(new BigDecimal(detail.getSpecWidth()));
        }

        // 3. 校验映射结果
        assertEquals(main.getOrderCode(), order.getErpOrderNo());
        assertEquals(0, order.getType());
        assertEquals(main.getCustomerCode(), order.getCustomerCode());
        assertEquals("PO-CUST-888", order.getCustomerOrderNo());
        assertEquals(main.getOrderTypeName(), order.getOrderTypeName());
        assertEquals("INV-001", order.getInventoryCode());
        assertEquals("CUST-MAT-999", order.getCustMaterialNo());
        assertEquals(new BigDecimal("0.35"), order.getSpecWidth());
        assertEquals(new BigDecimal("2.5"), order.getUnitWeight());
        assertEquals("35WW300", order.getMaterial());
        assertEquals("BOM-V1", order.getBom());
        assertEquals("冲压->退火->包装", order.getProcessRoute());
    }

    @Test
    @DisplayName("验证 U8 采购订单明细映射至 WMS 生产订单 (ProdOrder)")
    void testPurchaseOrderToProdOrderMapping() {
        // 1. 模拟构建 U8PurchaseOrderMain 与 U8PurchaseOrderDetail
        U8PurchaseOrderMain main = new U8PurchaseOrderMain();
        main.setOrderCode("PO20260824002");
        main.setVendorName("测试供应商B");
        main.setDepartmentName("采购部");

        U8PurchaseOrderDetail detail = new U8PurchaseOrderDetail();
        detail.setDetailId(20002L);
        detail.setInventoryCode("INV-002");
        detail.setInventoryName("硅钢原材料B");
        detail.setSpecification("SPEC-500");
        detail.setAuxiliaryQuantity(new BigDecimal("5.00"));
        detail.setQuantity(new BigDecimal("50.00"));
        detail.setRowMemo("采购加急");
        detail.setArriveDate("2026-09-05");
        detail.setPlannedCompletionDate("2026-09-03");
        detail.setSpecWidth("0.50");
        detail.setUnitWeight("3.0");
        detail.setMaterial("50WW400");
        detail.setBom("BOM-PUR-1");
        detail.setPackingRequirement("铁架包装");
        detail.setAnnealingMethod("真空退火");
        detail.setSprayCutting("激光切割");
        detail.setTechnicalRequirement("无硬蚀");
        detail.setPo("PO-SUP-2026");
        detail.setProcessRoute("剪切->包装");

        List<U8PurchaseOrderDetail> details = new ArrayList<>();
        details.add(detail);
        main.setDetails(details);

        // 2. 执行映射转换逻辑（采购订单 type=1）
        ProdOrder order = new ProdOrder();
        order.setErpOrderNo(main.getOrderCode());
        order.setType(1); // 1-采购订单
        order.setCustomerOrderNo(main.getCustomerOrderNo());

        order.setInventoryCode(detail.getInventoryCode());
        order.setInventoryName(detail.getInventoryName());
        order.setCustMaterialNo(detail.getSpecification());
        order.setM1(detail.getRowMemo());

        if (detail.getUnitWeight() != null) {
            order.setUnitWeight(new BigDecimal(detail.getUnitWeight()));
        }
        order.setMaterial(detail.getMaterial());
        order.setBom(detail.getBom());
        order.setPackingRequirement(detail.getPackingRequirement());
        order.setAnnealingMethod(detail.getAnnealingMethod());
        order.setSprayCutting(detail.getSprayCutting());
        order.setTechnicalRequirement(detail.getTechnicalRequirement());
        order.setPo(detail.getPo());
        order.setProcessRoute(detail.getProcessRoute());

        if (detail.getSpecWidth() != null) {
            order.setSpecWidth(new BigDecimal(detail.getSpecWidth()));
        }

        // 3. 校验映射结果
        assertEquals("PO20260824002", order.getErpOrderNo());
        assertEquals(1, order.getType());
        assertEquals("INV-002", order.getInventoryCode());
        assertEquals("SPEC-500", order.getCustMaterialNo());
        assertEquals("50WW400", order.getMaterial());
        assertEquals("BOM-PUR-1", order.getBom());
        assertEquals(new BigDecimal("0.50"), order.getSpecWidth());
        assertEquals(new BigDecimal("3.0"), order.getUnitWeight());
        assertEquals("剪切->包装", order.getProcessRoute());
    }
}
