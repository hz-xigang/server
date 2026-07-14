package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 扫描移库记录主表
 */
@Data
@TableName("stockMove")
public class StockMove extends TagEntity{

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
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
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常, 1:已删除)
     */
    private Integer deleted;

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
