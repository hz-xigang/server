package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 生产订单表(精简页面版)
 */
@Data
public class ProdOrderDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
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
     * 规格型号
     */
    private String spec;

    /**
     * 数据启用状态(1:启用, 0:停用)
     */
    private Boolean status;

    /**
     * WMS系统单据创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    /**
     * 类型：0-销售订单，1-采购订单
     */
    private Integer type;

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
     * 规格厚度
     */
    private BigDecimal specWidth;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedCompletionDate;

    /**
     * 预发货日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedDeliveryDate;

    /**
     * 打印纸箱标签模板
     */
    private String tempId;

}
