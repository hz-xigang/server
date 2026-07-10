package com.gz.xg.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户
 */
@Data
public class SysUserDto {

    /**
     * 主键
     */
    private String id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 类型
     * 1 - 后台账号
     * 2 - 工人账号
     */
    private Integer type;

    /**
     * WMS系统单据创建时间
     */
    private LocalDateTime createTime;

    /**
     * 自定义扩展备注字段1
     */
    private String m1;

    /**
     * 自定义扩展备注字段2
     */
    private String m2;

    /**
     * 自定义扩展备注字段3
     */
    private String m3;

    /**
     * 自定义扩展备注字段4
     */
    private String m4;

    /**
     * 自定义扩展备注字段5
     */
    private String m5;
}
