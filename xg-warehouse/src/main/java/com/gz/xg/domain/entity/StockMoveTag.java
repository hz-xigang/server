package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stockMoveTag")
public class StockMoveTag extends TagEntity {

    private String oLocId;

    private String oLocCode;

    /**
     * u8同步
     * 0- 未同步
     * 1- 已同步
     * 2- 不需同步
     */
    private Integer u8Sync;

}
