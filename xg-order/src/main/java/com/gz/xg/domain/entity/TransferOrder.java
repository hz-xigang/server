package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 车间返工/库间调拨-调拨指令主表
 */
@Data
@TableName("TransferOrder")
public class TransferOrder {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * 系统唯一或者对接用友ERP的调拨单号
     */
    private String orderNo;

    /**
     * 调拨业务单据生成的具体日期
     */
    private LocalDateTime orderDate;

    /**
     * 用友系统上游的调拨申请单号
     */
    private String applyNo;

    /**
     * 物料资产调出的源头部门名称
     */
    private String fromDept;

    /**
     * 物料资产调入的目标部门名称
     */
    private String toDept;

    /**
     * 调拨转出的物理仓库名称
     */
    private String fromWarehouse;

    /**
     * 调拨转入的目标仓库名称
     */
    private String toWarehouse;

    /**
     * 用友ERP定义的出库事务类别
     */
    private String outCategory;

    /**
     * 用友ERP定义的入库事务类别
     */
    private String inCategory;

    /**
     * 单据在ERP系统内的经手操作员
     */
    private String handler;

    /**
     * 用友ERP审核确认该单据的物理时间
     */
    private LocalDateTime auditTime;

    /**
     * 调拨业务类型分类标记(1代表调入入库, 2代表调出发出)
     */
    private Integer orderType;

    /**
     * 调拨业务的备注说明(例如：返工、车间转仓)
     */
    private String remark;

    /**
     * WMS系统内该单据记录的同步创建时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常状态, 1:已删除/作废)
     */
    private Integer deleted;

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
     * 状态
     * 0-创建
     * 1-完成
     */
    private Integer status;

}
