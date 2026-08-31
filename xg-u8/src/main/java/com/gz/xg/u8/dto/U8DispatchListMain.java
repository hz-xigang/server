package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友发货单主表数据
 */
@Data
public class U8DispatchListMain {

    /**
     * 单据主表主键
     */
    @SerializedName("id")
    private Long id;

    /**
     * 发货单据号
     */
    @SerializedName("ccode")
    private String orderCode;

    /**
     * 单据日期 (yyyy-MM-dd)
     */
    @SerializedName("ddate")
    private String orderDate;

    /**
     * 客户编码
     */
    @SerializedName("ccuscode")
    private String customerCode;

    /**
     * 客户名称
     */
    @SerializedName("ccusname")
    private String customerName;

    /**
     * 客户订单号/PO单号 (自定义项10)
     */
    @SerializedName("cdefine10")
    private String define10;

    /**
     * 是否含税 (自定义项3，如 "是" / "否")
     */
    @SerializedName("cdefine3")
    private String define3;

    /**
     * 销售类型编码
     */
    @SerializedName("cstcode")
    private String salesTypeCode;

    /**
     * 销售类型名称
     */
    @SerializedName("cstname")
    private String salesTypeName;

    /**
     * 部门编码
     */
    @SerializedName("cdepcode")
    private String departmentCode;

    /**
     * 部门名称
     */
    @SerializedName("cdepname")
    private String departmentName;

    /**
     * 业务员编码
     */
    @SerializedName("cpersoncode")
    private String personCode;

    /**
     * 业务员姓名
     */
    @SerializedName("cpersonname")
    private String personName;

    /**
     * 单据备注
     */
    @SerializedName("cmemo")
    private String memo;

    /**
     * 制单人
     */
    @SerializedName("cmaker")
    private String maker;

    /**
     * 审核人
     */
    @SerializedName("cverifier")
    private String verifier;

    /**
     * 审核日期
     */
    @SerializedName("cauditdate")
    private String auditDate;

    /**
     * 审核状态 (2:已审核)
     */
    @SerializedName("iverifystate")
    private Integer verifyState;

    /**
     * 审核时间 (ISO 格式)
     */
    @SerializedName("cAuditTime")
    private String auditTime;

    /**
     * 变更审核时间
     */
    @SerializedName("cChangAuditTime")
    private String changeAuditTime;

    /**
     * 时间戳版本
     */
    @SerializedName("ufts")
    private Long ufts;

    /**
     * 单据处理状态 (add/update/delete)
     */
    @SerializedName("voustate")
    private String voustate;

    /**
     * 发货单明细数据
     */
    @SerializedName("DataDetail")
    private List<U8DispatchListDetail> details;
}
