package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统菜单与权限资源表
 */
@Data
@TableName("SysRight")
public class SysRight {

    @TableId
    private String id;

    /**
     * 父级权限节点ID，根节点为NULL
     */
    private String parentId;

    private String menuName;

    /**
     * 权限资源类型：1-目录，2-菜单/页面，3-按钮，4-PDA模块
     */
    private Integer menuType;

    private String path;

    /**
     * 后端Shiro/Security拦截或前端控制的标识串
     */
    private String perms;

    private Integer sortNo;

    private String icon;

    private LocalDateTime createTime;

    private Integer deleted;

    private String m1;

    private String m2;

    private String m3;

    private String m4;

    private String m5;
}
