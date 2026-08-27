package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友调拨单明细数据
 */
@Data
public class U8TransferOrderDetail {

    /**
     * 调拨单号
     */
    @SerializedName("ctvcode")
    private String transferOrderCode;

    /**
     * 单据子表主键
     */
    @SerializedName("autoid")
    private Long autoId;

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
     * 辅计量数量
     */
    @SerializedName("inum")
    private BigDecimal auxiliaryQuantity;

    /**
     * 单位
     */
    @SerializedName("unitname")
    private String unitName;

    /**
     * 规格片宽
     */
    @SerializedName("cfree9")
    private String specWidth;

    /**
     * 客户编码
     */
    @SerializedName("cinvdefine2")
    private String customerCode;

    /**
     * 产品包装方式
     */
    @SerializedName("cfree8")
    private String packingMethod;

    /**
     * 数量
     */
    @SerializedName("iquantity")
    private BigDecimal quantity;

    /**
     * 批号
     */
    @SerializedName("cbatch")
    private String batchNo;

    /**
     * 货位
     */
    @SerializedName("InvPosition")
    private String invPosition;

    /**
     * 单据行备注
     */
    @SerializedName("cbmemo")
    private String rowMemo;
}
