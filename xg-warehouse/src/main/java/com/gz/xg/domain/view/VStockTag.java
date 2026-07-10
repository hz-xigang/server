package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("v_stockTag")
public class VStockTag extends  VProdTag{

    private String locId;

    private String locCode;

}
