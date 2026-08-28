package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 用友生产订单查询请求
 */
@Data
public class U8MomOrderQueryRequest {

    /**
     * 账套编号
     */
    @SerializedName("cacc_id")
    private String caccId;
}
