package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友销售订单明细
 */
@Data
public class U8SalesOrderDetail {

    /**
     * 订单子表主键（用于与发货单关联）
     */
    @SerializedName("isosid")
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
     * 规格型号
     */
    @SerializedName("cinvstd")
    private String specification;

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
     * 计划发货日期
     */
    @SerializedName("dpreDate")
    private String deliveryDate;
}
