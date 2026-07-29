package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("v_moveTag")
public class VMoveTag extends VProdTag{

    private String oLocCode;

}
