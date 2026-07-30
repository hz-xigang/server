package com.gz.xg.domain.dto;

import lombok.Data;

/**
 * 库位物料汇总 DTO
 */
@Data
public class VLocInventorySummaryDto {

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
     */
    private Integer qty;

    /**
     * 生产单数量（不同生产单号的数量）
     */
    private Integer prodQty;
}
