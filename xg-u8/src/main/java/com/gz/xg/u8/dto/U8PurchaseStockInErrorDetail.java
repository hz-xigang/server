package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用友采购入库明细同步错误信息
 */
@Data
public class U8PurchaseStockInErrorDetail {

    /**
     * 单据号
     */
    @SerializedName("ccode")
    private String orderCode;

    /**
     * 行号
     */
    @SerializedName("irow")
    private Integer rowNo;

    /**
     * 错误信息详情
     */
    @SerializedName("error_msg")
    private String errorMsg;
}
