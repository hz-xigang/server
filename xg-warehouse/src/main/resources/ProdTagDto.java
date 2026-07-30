package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 纸箱单品标签打印记录与流水表
 */
@Data
public class ProdTagDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 条码号
     */
    private String tagNo;

    /**
     * 关联的生产订单表主键ID
     */
    private String prodOrderId;

    /**
     * 单箱物料数量
     */
    private Integer qty;

    /**
     * 货品毛重(kg)，保留两位小数
     */
    private BigDecimal grossWeight;

    /**
     * 货品净重(kg)，保留两位小数
     */
    private BigDecimal netWeight;




    /**
     * 标签打印生成时间
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

    private String username;

    private String userId;

    private String realName;
}
