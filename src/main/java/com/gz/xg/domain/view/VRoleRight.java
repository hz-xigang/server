package com.gz.xg.domain.view;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("v_RoleRight")
public class VRoleRight {

    private String rightId;

    private String parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String perms;

    private String icon;

    private LocalDateTime createTime;

    private String roleName;

    private String roleId;
}
