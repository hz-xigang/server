package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扫描移库记录主表
 */
@Data
public class StockMoveDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * WMS系统生成的唯一移库单号(通过流水号表派生)
     */
    private String receiptNo;

    /**
     * 本次扫描移库的物料总数量
     */
    private Integer qty;


    /**
     * PDA端确认提交移库的时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 移库总货品毛重(kg)
     */
    private BigDecimal grossWeight;

    /**
     * 移库总货品净重(kg)
     */
    private BigDecimal netWeight;

    /**
     * 自定义扩展备注字段1
     */
    private String m1;

    /**
     * 自定义扩展备注字段2
     */
    private String m2;

    /**
     * 自定义扩展备注字段3
     */
    private String m3;

    /**
     * 自定义扩展备注字段4
     */
    private String m4;

    /**
     * 自定义扩展备注字段5
     */
    private String m5;

    /**
     * 库位Id
     */
    private String locId;

    /**
     * 库位编号
     */
    private String locCode;

    private String username;

    private String userId;

    private String type;

}
