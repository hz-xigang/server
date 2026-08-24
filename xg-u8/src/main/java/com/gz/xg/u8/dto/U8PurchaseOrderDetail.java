package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友采购订单明细
 */
@Data
public class U8PurchaseOrderDetail {

    /**
     * 订单明细主键（用于生成采购入库单）
     */
    @SerializedName("iposid")
    private Long detailId;

    /**
     * 存货编码
     */
    @SerializedName("cinvcode")
    private String inventoryCode;

    /**
     * 存货名称
     */
    @SerializedName("cinvname")
    private String inventoryName;

    /**
     * 规格型号 / 客户料号
     */
    @SerializedName("cinvstd")
    private String specification;

    /**
     * 客户料号（对齐销售订单明细字段名）
     */
    @SerializedName("cinvstd")
    private String customerMaterialNo;

    /**
     * 辅计量数量
     */
    @SerializedName("inum")
    private BigDecimal auxiliaryQuantity;

    /**
     * 数量
     */
    @SerializedName("iquantity")
    private BigDecimal quantity;

    /**
     * 单据行备注
     */
    @SerializedName("cbmemo")
    private String rowMemo;

    /**
     * 计划到货日期
     */
    @SerializedName("darrivedate")
    private String arriveDate;

    /**
     * 预完工日期
     */
    @SerializedName("dpremodate")
    private String plannedCompletionDate;

    /**
     * 规格片宽
     */
    @SerializedName("cfree9")
    private String specWidth;

    /**
     * 单重
     */
    @SerializedName("iinvweight")
    private String unitWeight;

    /**
     * 产品材质
     */
    @SerializedName("cinvdefine1")
    private String material;

    /**
     * BOM
     */
    @SerializedName("cdefine23")
    private String bom;

    /**
     * 包装要求
     */
    @SerializedName("cfree8")
    private String packingRequirement;

    /**
     * 退火方式
     */
    @SerializedName("cinvdefine9")
    private String annealingMethod;

    /**
     * 喷涂切割
     */
    @SerializedName("cinvdefine10")
    private String sprayCutting;

    /**
     * 技术要求
     */
    @SerializedName("cinvdefine8")
    private String technicalRequirement;

    /**
     * PO
     */
    @SerializedName("cdefine32")
    private String po;

    /**
     * 工艺路线
     */
    @SerializedName("cdefine28")
    private String processRoute;
}
