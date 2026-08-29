package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用友移库作业明细数据
 */
@Data
public class U8StockMoveDetailRequest {

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
     * 转出货位
     */
    @SerializedName("invposition_out")
    private String invPositionOut;

    /**
     * 转入货位
     */
    @SerializedName("invposition_in")
    private String invPositionIn;

    /**
     * 包装方式
     */
    @SerializedName("cfree8")
    private String packingMethod;

    /**
     * 规格片宽
     */
    @SerializedName("cfree9")
    private String specWidth;

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
}
