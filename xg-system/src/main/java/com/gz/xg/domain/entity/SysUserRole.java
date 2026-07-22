package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("SysUserRole")
public class SysUserRole {

    private String roleId;

    private String userId;
}
