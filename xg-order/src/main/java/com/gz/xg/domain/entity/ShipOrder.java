package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 外部销售出库-发货指令单表
 */
@Data
@TableName("shipOrder")
public class ShipOrder {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * WMS系统唯一发货指令单号(可对应流水号表)
     */
    private String shipNo;

    /**
     * 发货类型说明
     */
    private String type;

    /**
     * 销售类型说明
     */
    private String salesType;

    /**
     * 对应对接用友ERP的已审核销售单号/发货单号
     */
    private String erpOrderNo;

    /**
     * 价格是否含税(1:含税, 0:未税)
     */
    private Boolean isTax;

    /**
     * 用友系统同步过来的销售部门名称
     */
    private String salesDept;

    /**
     * 物流发运方式说明
     */
    private String dispatchMode;

    /**
     * 用友系统的客户唯一编码
     */
    private String customerCode;

    /**
     * 收货联系人姓名
     */
    private String contactPerson;

    /**
     * 负责该单据的用友销售业务员姓名
     */
    private String salesman;

    /**
     * 单据同步或创建时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常单据, 1:已撤销/软删除)
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
