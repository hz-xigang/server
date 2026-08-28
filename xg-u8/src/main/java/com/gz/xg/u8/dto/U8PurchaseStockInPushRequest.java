package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友采购入库接口统一请求封装
 */
@Data
public class U8PurchaseStockInPushRequest {

    /**
     * 账套号 (必填)
     */
    @SerializedName("cacc_id")
    private String caccId;

    /**
     * 入库单主表数据
     */
    @SerializedName("Data")
    private List<U8PurchaseStockInRequest> data;
}
