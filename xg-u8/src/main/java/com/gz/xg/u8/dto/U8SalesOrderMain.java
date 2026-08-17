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
    @SerializedName("cvencode")
    private String customerCode;

    /**
     * 客户名称
     */
    @SerializedName("cvenname")
    private String customerName;

    /**
     * 销售类型
     */
    @SerializedName("cptcode")
    private String salesTypeCode;

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
