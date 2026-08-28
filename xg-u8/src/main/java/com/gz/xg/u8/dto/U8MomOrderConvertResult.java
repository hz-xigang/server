package com.gz.xg.u8.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 用友生产订单转换为系统生产订单的DTO
 */
@Data
public class U8MomOrderConvertResult {

    /**
     * ERP单据号
     */
    private String erpOrderNo;

    /**
     * ERP明细主键 (modid)
     */
    private String erpOrderId;

    /**
     * 类型：2-生产订单
     */
    private Integer type = 2;

    /**
     * 存货编码
     */
    private String inventoryCode;

    /**
     * 存货名称
     */
    private String inventoryName;

    /**
     * 规格型号
     */
    private String spec;

    /**
     * 规格片宽
     */
    private String specWidth;

    /**
     * 包装要求
     */
    private String packingRequirement;

    /**
     * 数量 (iquantity)
     */
    private BigDecimal qty;

    /**
     * 辅计量数量 (inum)
     */
    private BigDecimal inNum;

    /**
     * 扩展字段1: 自定义扩展
     */
    private String m1;

    /**
     * 扩展字段2: 生产部门
     */
    private String m2;

    /**
     * 扩展字段3: 仓库
     */
    private String m3;

    /**
     * 扩展字段4: 明细行备注
     */
    private String m4;

    /**
     * 扩展字段5: 单据总备注
     */
    private String m5;
}
