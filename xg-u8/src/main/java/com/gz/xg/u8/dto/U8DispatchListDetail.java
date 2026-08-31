package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友发货单明细数据
 */
@Data
public class U8DispatchListDetail {

    /**
     * 发货单主表ID
     */
    @SerializedName("id")
    private Long id;

    /**
     * 销售订单行主键 (用于关联销售订单及成品出库)
     */
    @SerializedName("isosid")
    private Long salesOrderDetailId;

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
     * 存货自定义项23
     */
    @SerializedName("cdefine23")
    private String define23;

    /**
     * 客户编码
     */
    @SerializedName("cinvdefine2")
    private String customerCode;

    /**
     * 规格片宽
     */
    @SerializedName("cfree9")
    private String specWidth;

    /**
     * 材质 (存货自定义项1)
     */
    @SerializedName("cinvdefine1")
    private String material;

    /**
     * 单重
     */
    @SerializedName("iinvweight")
    private BigDecimal unitWeight;

    /**
     * 包装方式
     */
    @SerializedName("cfree8")
    private String packingMethod;

    /**
     * 退火方式 (存货自定义项9)
     */
    @SerializedName("cinvdefine9")
    private String annealingMethod;

    /**
     * 喷涂线切割 (存货自定义项10)
     */
    @SerializedName("cinvdefine10")
    private String sprayCutting;

    /**
     * 技术要求 (存货自定义项8)
     */
    @SerializedName("cinvdefine8")
    private String technicalRequirement;

    /**
     * 存货自定义项32
     */
    @SerializedName("cdefine32")
    private String define32;

    /**
     * 存货自定义项28
     */
    @SerializedName("cdefine28")
    private String define28;

    /**
     * 单位
     */
    @SerializedName("unitname")
    private String unitName;

    /**
     * 辅计量数量 / 重量
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
     * 行号
     */
    @SerializedName("irowno")
    private Integer rowNo;
}
