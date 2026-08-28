package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用友销售订单查询请求
 */
@Data
public class U8SoMainQueryRequest {

    /**
     * 账套编号
     */
    @SerializedName("cacc_id")
    private String caccId;
}
