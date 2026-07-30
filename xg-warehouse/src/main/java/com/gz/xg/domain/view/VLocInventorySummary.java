package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 库位物料汇总视图
 * 对应数据库视图：v_LocInventorySummary
 */
@Data
@TableName("v_LocInventorySummary")
public class VLocInventorySummary {

    /**
     * 库位主键ID
     */
    private String id;

    /**
     * 库位编码
     */
    private String locCode;

    /**
     * 库位类型
     */
    private String locType;

    /**
     * 库存总数量（纸箱数）
     * 来源：StockInventory 表 qty 字段求和
     */
    private Integer qty;

    /**
     * 生产单数量（不同生产单号的数量）
     * 来源：StockInventory 关联 v_prodTag，按 locId 统计不同 prodNo 的数量
     */
    private Integer prodQty;
}
