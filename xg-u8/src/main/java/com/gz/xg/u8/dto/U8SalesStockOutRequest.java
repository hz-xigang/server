package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友成品出仓主表数据
 */
@Data
public class U8SalesStockOutRequest {

    /**
     * 单据号 (必填)
     */
    @SerializedName("ccode")
    private String orderCode;

    /**
     * 单据日期 (必填，格式: yyyy-MM-dd)
     */
    @SerializedName("ddate")
    private String orderDate;

    /**
     * 单据备注
     */
    @SerializedName("cmemo")
    private String memo;

    /**
     * 制单人 (必填)
     */
    @SerializedName("cmaker")
    private String maker;

    /**
     * 审核人 (必填)
     */
    @SerializedName("chandler")
    private String handler;

    /**
     * 审核日期 (必填，格式: yyyy-MM-dd)
     */
    @SerializedName("dveridate")
    private String verifyDate;

    /**
     * 仓库编码 (必填)
     */
    @SerializedName("cwhcode")
    private String warehouseCode;

    /**
     * 出库明细数据
     */
    @SerializedName("details")
    private List<U8SalesStockOutDetailRequest> details;
}
