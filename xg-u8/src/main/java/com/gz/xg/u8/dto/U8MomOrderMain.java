package com.gz.xg.u8.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 用友生产订单主表
 */
@Data
public class U8MomOrderMain {

    /**
     * 单据处理状态（add: 新增, update: 修改, delete: 删除）
     */
    @SerializedName("voustate")
    private String voustate;

    /**
     * 订单主表主键
     */
    @SerializedName("moid")
    private Integer mainId;

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
    private List<U8MomOrderDetail> details;
}
