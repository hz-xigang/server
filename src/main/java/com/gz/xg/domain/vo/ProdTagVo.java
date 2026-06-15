package com.gz.xg.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("v_prodTag")
public class ProdTagVo {

    private String id;

    private String tagNo;

    private String prodOrderId;

    private BigDecimal grossWeight;

    private BigDecimal netWeight;

    private String printUser;

    private String productCategory;

    private BigDecimal qty;

    private String prodNo;

    private LocalDateTime createTime;

}
