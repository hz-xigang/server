package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友成品出仓接口统一请求封装
 */
@Data
public class U8SalesStockOutPushRequest {

    /**
     * 账套号 (必填)
     */
    @SerializedName("cacc_id")
    private String caccId;

    /**
     * 出库单主表数据
     */
    @SerializedName("Data")
    private List<U8SalesStockOutRequest> data;
}
