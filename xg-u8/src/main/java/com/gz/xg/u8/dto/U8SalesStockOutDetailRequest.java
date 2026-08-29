package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友成品出仓明细数据
 */
@Data
public class U8SalesStockOutDetailRequest {

    /**
     * 行号 (必填)
     */
    @SerializedName("irow")
    private Integer rowNo;

    /**
     * 存货编码 (必填)
     */
    @SerializedName("cinvcode")
    private String inventoryCode;

    /**
     * 存货名称 (必填)
     */
    @SerializedName("cinvname")
    private String inventoryName;

    /**
     * 规格型号 (必填)
     */
    @SerializedName("cinvstd")
    private String spec;

    /**
     * 材质 (必填)
     */
    @SerializedName("cinvdefine1")
    private String material;

    /**
     * 包装方式 (必填)
     */
    @SerializedName("cfree8")
    private String packingMethod;

    /**
     * 规格片宽 (必填)
     */
    @SerializedName("cfree9")
    private String specWidth;

    /**
     * 客户编码 (必填)
     */
    @SerializedName("cinvdefine2")
    private String customerCode;

    /**
     * 单重
     */
    @SerializedName("iinvweight")
    private BigDecimal unitWeight;

    /**
     * 总重量
     */
    @SerializedName("weight")
    private BigDecimal totalWeight;

    /**
     * 存货代码 (必填)
     */
    @SerializedName("cinvaddcode")
    private String invAddCode;

    /**
     * 单位
     */
    @SerializedName("unitname")
    private String unitName;

    /**
     * 辅计量数量 (双计量物料必填)
     */
    @SerializedName("inum")
    private BigDecimal auxiliaryQuantity;

    /**
     * 数量 (必填)
     */
    @SerializedName("iquantity")
    private BigDecimal quantity;

    /**
     * 货位
     */
    @SerializedName("invposition")
    private String invPosition;

    /**
     * 批号 (批次管理物料必填)
     */
    @SerializedName("cbatch")
    private String batchNo;

    /**
     * 单据行备注
     */
    @SerializedName("cbmemo")
    private String rowMemo;

    /**
     * 销售订单行主键 (必填，用于与销售订单关联)
     */
    @SerializedName("isosid")
    private String salesOrderDetailId;
}
