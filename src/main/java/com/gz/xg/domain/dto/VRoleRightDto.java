package com.gz.xg.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VRoleRightDto {

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
