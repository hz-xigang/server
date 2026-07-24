package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("v_prodTag")
public class VProdTag {

    private String id;

    private String tagNo;

    private String prodOrderId;

    private BigDecimal grossWeight;

    private BigDecimal netWeight;


    private String productCategory;

    private Integer qty;

    private String prodNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String spec;

    private String customerCode;

    private String inventoryCode;

    private String inventoryName;

    private String username;

    private String userId;

    private Integer deleted;

}
