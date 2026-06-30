package com.gz.xg.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车间返工/库间调拨-调拨指令明细表
 */
@Data
public class TransferOrderDetailDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 关联的调拨主表(TransferOrder)唯一主键ID
     */
    private String pId;

    /**
     * 调拨产品批号
     */
    private String batchNo;

    /**
     * 用友系统的存货编码
     */
    private String inventoryCode;

    /**
     * 用友系统的存货名称
     */
    private String inventoryName;

    /**
     * 产品的规格型号说明
     */
    private String spec;

    /**
     * 库存计量与调拨结算的单位
     */
    private String unit;

    /**
     * 单项计划调拨的物料数量
     */
    private BigDecimal qty;

    /**
     * 单项计划调拨的物料总重量
     */
    private BigDecimal weight;

    /**
     * 货品规格片宽尺寸说明
     */
    private String specWidth;

    /**
     * 用友系统的客户唯一编码
     */
    private String customerCode;

    /**
     * 该批成品所追踪追溯的源头母材批号
     */
    private String pBatchNo;

    /**
     * 用友系统同步或指定的包装方式
     */
    private String packingMethod;

    /**
     * 明细单据记录的创建或同步时间
     */
    private LocalDateTime createTime;

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
