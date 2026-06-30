package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShipOrderDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;
    /**
     * WMS系统唯一发货指令单号(可对应流水号表)
     */
    private String no;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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

    private List<ShipOrderDetailDto> details;

}
