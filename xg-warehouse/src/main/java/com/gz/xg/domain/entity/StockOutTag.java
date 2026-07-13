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

}
