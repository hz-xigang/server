package com.gz.xg.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扫描入库记录主表
 */
@Data
public class StockInDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * WMS系统生成的唯一入库单号(通过流水号表派生)
     */
    private String receiptNo;

    /**
     * 本次扫描入库的物料总数量
     */
    private Integer qty;

    /**
     * 手持PDA端执行扫描入库操作的工人姓名
     */
    private String printUser;

    /**
     * PDA端确认提交入库的时间
     */
    private LocalDateTime createTime;

    /**
     * 入库总货品毛重(kg)
     */
    private BigDecimal grossWeight;

    /**
     * 入库总货品净重(kg)
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
}
