package com.gz.xg.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 外部销售出库-发货指令单明细表
 */
@Data
public class ShipOrderDetailDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 关联的发货单主表(ShippingInstruction)唯一主键ID
     */
    private String pId;

    /**
     * 对应对接用友ERP的源头订单号
     */
    private String orderNo;

    /**
     * 货品的产品批号
     */
    private String batchNo;

    /**
     * 执行出库作业的物理仓库名称
     */
    private String warehouseName;

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
    private BigDecimal qty;

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
