package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class U8OtherStockOutPushRequest {

    @SerializedName("cacc_id")
    private String caccId;

    @SerializedName("Data")
    private List<U8OtherStockOutRequest> data;
}
