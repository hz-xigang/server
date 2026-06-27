package com.gz.xg.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("v_stockTag")
public class StockTagVo extends  ProdTagVo{

    private String locId;

    private String locCode;

}
