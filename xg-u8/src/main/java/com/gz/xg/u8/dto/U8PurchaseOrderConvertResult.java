package com.gz.xg.u8.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * U8 采购订单转换结果 DTO
 */
@Data
public class U8PurchaseOrderConvertResult {
    private String erpOrderNo;
    private Integer type;
    private String customerCode;
    private String customerOrderNo;
    private String orderTypeName;
    private String saleType;
    private String salesperson;
    private String inventoryCode;
    private String inventoryName;
    private String custMaterialNo;
    private String m1;
    private BigDecimal unitWeight;
    private String material;
    private String bom;
    private String packingRequirement;
    private String annealingMethod;
    private String sprayCutting;
    private String technicalRequirement;
    private String po;
    private String processRoute;
    private String specWidth;
    private String erpOrderId;
    /**
     * 数量 (iquantity)
     */
    private BigDecimal qty;

    /**
     * 辅计量数量 (inum)
     */
    private BigDecimal inNum;
}
