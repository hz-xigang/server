package com.gz.xg.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发货记录表
 */
@Data
public class ShipRecordDto {

    private String id;

    /**
     * 指令单
     */
    private String shipOrder;

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
     * 产品材质属性
     */
    private String material;

    /**
     * 库存盘点与出库的库存计量单位
     */
    private String unit;

    /**
     * 计划发货的物料数量
     */
    private Integer qty;

    /**
     * 计划发货的物料总重量
     */
    private BigDecimal weight;

    /**
     * 单件货品的单品单重
     */
    private BigDecimal unitWeight;

    /**
     * 用友系统同步或指定的包装方式
     */
    private String packingMethod;

    /**
     * 货品规格片宽尺寸说明
     */
    private String specWidth;

    /**
     * 用友系统的客户唯一编码
     */
    private String customerCode;

    /**
     * 明细单据记录的创建或同步时间
     */
    private LocalDateTime createTime;

    private String m1;

    private String m2;

    private String m3;

    private String m4;

    private String m5;

    /**
     * 出库单号
     */
    private String outNo;

    private String username;

    private String userId;

}
