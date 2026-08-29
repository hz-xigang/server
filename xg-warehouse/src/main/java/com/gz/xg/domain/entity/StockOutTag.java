package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stockOutTag")
public class StockOutTag extends TagEntity {

    /**
     * 库位Id
     */
    private String locId;

    /**
     * 库位编号
     */
    private String locCode;

    /**
     * u8同步
     * 0- 未同步
     * 1- 已同步
     * 2- 不需同步
     */
    private Integer u8Sync;

}
