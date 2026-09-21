package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class U8OtherStockOutDetailRequest {

    @SerializedName("irow")
    private Integer rowNo;

    @SerializedName("cinvcode")
    private String inventoryCode;

    @SerializedName("inum")
    private BigDecimal auxiliaryQuantity;

    @SerializedName("iquantity")
    private BigDecimal quantity;

    @SerializedName("invposition")
    private String invPosition;

    @SerializedName("cbatch")
    private String batchNo;

    @SerializedName("cbmemo")
    private String rowMemo;
}
