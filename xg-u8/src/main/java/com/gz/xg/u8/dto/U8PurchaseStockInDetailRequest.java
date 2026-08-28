package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用友采购入库明细请求DTO
 */
@Data
public class U8PurchaseStockInDetailRequest {

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
     * 采购订单行主键 (必填，用于与采购订单关联)
     */
    @SerializedName("iposid")
    private String purchaseOrderDetailId;
}
