package com.gz.xg.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产线或出库备料-备料指令主表
 */
@Data
public class PrepOrderDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * WMS系统唯一备料指令单号(通过流水号表派生)
     */
    private String prepNo;

    /**
     * 用友系统同步或手工录入的客户唯一编码
     */
    private String customerCode;

    /**
     * 要求的货品包装方式(如:纸箱、木箱、托盘裸装)
     */
    private String packingMethod;

    /**
     * 物流发运方式说明(如:汽运自提、顺丰速运)
     */
    private String dispatchMode;

    /**
     * 在后台系统创建或同步该备料单的制单人姓名
     */
    private String creator;

    /**
     * 排程计划要求的最佳发货装车时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 备料业务单据的说明信息
     */
    private String remark;

    /**
     * 单据贸易类型分类标记(1代表国内单, 2代表外贸单)
     */
    private Integer orderType;

    /**
     * 手持PDA端执行的备料状态(0=未开始, 1=备料中, 2=已备料)
     */
    private Integer status;

    /**
     * WMS系统内该单据记录的同步创建时间
     */
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

    private List<PrepOrderDetailDto> details;
}
