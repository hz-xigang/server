package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友调拨单主表数据
 */
@Data
public class U8TransferOrderMain {

    /**
     * 单据处理状态：add 新增，update 修改，delete 删除
     */
    @SerializedName("voustate")
    private String voucherState;

    /**
     * 单据主表主键
     */
    @SerializedName("id")
    private Long transferOrderId;

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
     * 转出仓库编码
     */
    @SerializedName("cowhcode")
    private String fromWarehouseCode;

    /**
     * 转出仓库名称
     */
    @SerializedName("cowhname")
    private String fromWarehouseName;

    /**
     * 转入仓库编码
     */
    @SerializedName("ciwhcode")
    private String toWarehouseCode;

    /**
     * 转入仓库名称
     */
    @SerializedName("ciwhname")
    private String toWarehouseName;

    /**
     * 转出部门编码
     */
    @SerializedName("codepcode")
    private String fromDepartmentCode;

    /**
     * 转出部门名称
     */
    @SerializedName("codepname")
    private String fromDepartmentName;

    /**
     * 转入部门编码
     */
    @SerializedName("cidepcode")
    private String toDepartmentCode;

    /**
     * 转入部门名称
     */
    @SerializedName("cidepname")
    private String toDepartmentName;

    /**
     * 入库类别编码
     */
    @SerializedName("cirdcode")
    private String inCategoryCode;

    /**
     * 入库类别名称
     */
    @SerializedName("cirdname")
    private String inCategoryName;

    /**
     * 出库类别编码
     */
    @SerializedName("cordcode")
    private String outCategoryCode;

    /**
     * 出库类别名称
     */
    @SerializedName("cordname")
    private String outCategoryName;

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
    private List<U8TransferOrderDetail> details;
}
