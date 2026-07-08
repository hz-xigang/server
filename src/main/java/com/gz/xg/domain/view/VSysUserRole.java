package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("v_SysUserRole")
public class VSysUserRole {

    private String roleId;

    private String roleName;

    private String remark;

    private String userId;

    private String username;

    private String realName;
}
