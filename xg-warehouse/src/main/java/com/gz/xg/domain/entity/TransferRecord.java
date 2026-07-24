package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 调拨记录
 */
@Data
@TableName("TransferRecord")
public class TransferRecord {

    @TableId
    private String id;

    /**
     * 指令单
     */
    private String transferOrderNo;

    /**
     * 调拨产品批号
     */
    private String batchNo;

    /**
     * 用友系统的存货编码
     */
    private String inventoryCode;

    /**
     * 用友系统的存货名称
     */
    private String inventoryName;

    /**
     * 产品的规格型号说明
     */
    private String spec;

    /**
     * 库存计量与调拨结算的单位
     */
    private String unit;

    /**
     * 单项计划调拨的物料数量
     */
    private Integer qty;

    /**
     * 单项计划调拨的物料总重量
     */
    private BigDecimal weight;

    /**
     * 货品规格片宽尺寸说明
     */
    private String specWidth;

    /**
     * 用友系统的客户唯一编码
     */
    private String customerCode;

    /**
     * 该批成品所追踪追溯的源头母材批号
     */
    private String pBatchNo;

    /**
     * 用友系统同步或指定的包装方式
     */
    private String packingMethod;

    /**
     * 创建或同步时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常明细, 1:已删除)
     */
    private Integer deleted;

    private String m1;

    private String m2;

    private String m3;

    private String m4;

    private String m5;

    /**
     * 条码
     */
    private String tagNo;

    private String username;

    private String userId;

    private String realName;
}
