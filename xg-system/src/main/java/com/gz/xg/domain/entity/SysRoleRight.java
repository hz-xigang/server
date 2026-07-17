package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SysRoleRight")
public class SysRoleRight {

    private String roleId;

    private String rightId;

    private Integer rightType;

}
