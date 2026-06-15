package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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
     * 规格型号
     */
    private String spec;


    /**
     * 软删除
     */
    private Boolean deleted;

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
     * 类型
     */
    private Integer type;

}
