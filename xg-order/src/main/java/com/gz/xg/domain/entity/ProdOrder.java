package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产订单表(精简页面版)
 */
@Data
@TableName("prodOrder")
public class ProdOrder {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * 自动生成的系统唯一生产单号
     */
    private String prodNo;

    /**
     * 对应对接用友的销售单号/采购单号
     */
    private String erpOrderNo;

    /**
     * 客户订单号
     */
    private String customerOrderNo;

    /**
     * 订单类型
     */
    private String orderTypeName;

    /**
     * 销售类型
     */
    private String saleType;

    /**
     * 业务员
     */
    private String salesperson;

    /**
     * 用友系统的存货编码
     */
    private String inventoryCode;

    /**
     * 用友系统的存货名称
     */
    private String inventoryName;

    /**
     * 用友系统的客户编码
     */
    private String customerCode;

    /**
     * 产品类别(手工填写)
     */
    private String productCategory;



    /**
     * BOM
     */
    private String bom;

    /**
     * 包装要求
     */
    private String packingRequirement;

    /**
     * 退火方式
     */
    private String annealingMethod;

    /**
     * 喷涂切割
     */
    private String sprayCutting;

    /**
     * 技术要求
     */
    private String technicalRequirement;

    /**
     * PO
     */
    private String po;

    /**
     * 工艺路线
     */
    private String processRoute;

    /**
     * 预完工日期
     */
    private LocalDate plannedCompletionDate;

    /**
     * 预发货日期
     */
    private LocalDate plannedDeliveryDate;

    /**
     * WMS系统单据创建时间
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

    /**
     * 类型：0-销售订单，1-采购订单 2-生产订单
     */
    private Integer type;

    /**
     * 规格厚度
     */
    private String specWidth;

    /**
     * 单位重量
     */
    private BigDecimal unitWeight;

    /**
     * 材料
     */
    private String material;

    /**
     * 客户料号
     */
    private String custMaterialNo;

    /**
     * 打印纸箱标签模板
     */
    private String tempId;


    private Integer deleted;

    private String spec;

    /**
     * erp单号id
     */
    private String erpOrderId;

    private BigDecimal qty;

    private BigDecimal inNum;
}
