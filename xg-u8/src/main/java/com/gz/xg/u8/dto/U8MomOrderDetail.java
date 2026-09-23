package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友生产订单明细
 */
@Data
public class U8MomOrderDetail {

    /**
     * 订单子表主键（用于生成产成品入库单）
     */
    @SerializedName("modid")
    private Integer detailId;

    /**
     * 行号
     */
    @SerializedName("SortSeq")
    private Integer sortSeq;

    /**
     * 部门编码
     */
    @SerializedName("cdepcode")
    private String departmentCode;

    /**
     * 部门名称
     */
    @SerializedName("cdepname")
    private String departmentName;

    /**
     * 仓库编码
     */
    @SerializedName("cwhcode")
    private String warehouseCode;

    /**
     * 仓库名称
     */
    @SerializedName("cwhname")
    private String warehouseName;

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
     * 规格型号
     */
    @SerializedName("cinvstd")
    private String spec;

    /**
     * 规格片宽
     */
    @SerializedName("cfree9")
    private String specWidth;

    /**
     * 产品包装方式
     */
    @SerializedName("cfree8")
    private String packingMethod;

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
     * 客户编号
     */
    @SerializedName("cinvdefine2")
    private String customerCode;

    /**
     * 产品材质/材料
     */
    @SerializedName("cinvdefine1")
    private String material;

    /**
     * 技术要求
     */
    @SerializedName("cinvdefine8")
    private String technicalRequirement;

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
     * 单重
     */
    @SerializedName("iinvweight")
    private String unitWeight;

    /**
     * 辅计量单位名称
     */
    @SerializedName("unitname")
    private String unitName;

    /**
     * 单据行备注
     */
    @SerializedName("cbmemo")
    private String rowMemo;

    @SerializedName("erpOrderNo")
    private String erpOrderNo;

}
