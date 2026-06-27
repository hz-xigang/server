package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 核心业务-实时动态库存表
 */
@Data
@TableName("stockInventory")
public class StockInventory {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * 在库的唯一纸箱标签条码号
     */
    private String tagNo;

    /**
     * 货品当前存放的数字化库位编码(如:A-01-02)
     */
    private String locCode;

    /**
     * 当前库位ID
     */
    private String locId;

    /**
     * 当前库位该条码的在库实物数量
     */
    private Integer qty;

    /**
     * 单箱货品毛重(kg)
     */
    private BigDecimal grossWeight;

    /**
     * 单箱货品净重(kg)
     */
    private BigDecimal netWeight;

    /**
     * 该条码首次上架入库的物理时间
     */
    private LocalDateTime createTime;

    /**
     * 该条码最后一次发生变动(如PDA移库完成)的时间
     */
    private LocalDateTime updateTime;

    /**
     * 软删除逻辑标记(0:正常在库, 1:已出库核销/删除)
     */
    private Integer deleted;

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
