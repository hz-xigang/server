package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class U8OtherStockOutRequest {

    @SerializedName("ccode")
    private String orderCode;

    @SerializedName("ddate")
    private String orderDate;

    @SerializedName("cmemo")
    private String memo;

    @SerializedName("cmaker")
    private String maker;

    @SerializedName("chandler")
    private String handler;

    @SerializedName("dveridate")
    private String verifyDate;

    @SerializedName("crdcode")
    private String rdCode;

    @SerializedName("cwhcode")
    private String warehouseCode;

    @SerializedName("details")
    private List<U8OtherStockOutDetailRequest> details;
}
