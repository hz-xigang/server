package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友采购订单主表数据
 */
@Data
public class U8PurchaseOrderMain {

    /**
     * 单据处理状态：add 新增，update 修改，delete 删除
     */
    @SerializedName("voustate")
    private String voucherState;

    /**
     * 订单主表主键
     */
    @SerializedName("poid")
    private Long purchaseOrderId;

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
     * 供应商编码
     */
    @SerializedName("cvencode")
    private String vendorCode;

    /**
     * 供应商名称
     */
    @SerializedName("cvenname")
    private String vendorName;

    /**
     * 采购类型
     */
    @SerializedName("cptcode")
    private String purchaseTypeCode;

    /**
     * 采购类型名称
     */
    @SerializedName("cptname")
    private String purchaseTypeName;

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
     * 业务员名称
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
    @SerializedName("DataDetail")
    private List<U8PurchaseOrderDetail> details;
}
