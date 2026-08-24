package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友销售订单主表数据
 */
@Data
public class U8SalesOrderMain {

    /**
     * 单据处理状态：add 新增，update 修改，delete 删除
     */
    @SerializedName("voustate")
    private String voucherState;

    /**
     * 订单主表主键
     */
    @SerializedName("id")
    private Long salesOrderId;

    /**
     * 单据号
     */
    @SerializedName("ccode")
    private String orderCode;

    /**
     * 单据日期
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
     * 客户订单号
     */
    @SerializedName("cdefine10")
    private String customerOrderNo;

    /**
     * 订单类型
     */
    @SerializedName("cdefine3")
    private String orderTypeName;

    /**
     * 业务员编码
     */
    @SerializedName("cpersoncode")
    private String salespersonCode;

    /**
     * 业务员
     */
    @SerializedName("cpersonname")
    private String salesperson;

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
     * 订单明细数据
     */
    @SerializedName("details")
    private List<U8SalesOrderDetail> details;
}
