package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色表
 */
@Data
@TableName("SysRole")
public class SysRole {

    @TableId
    private String id;

    /**
     * 角色名称(如：仓库主管、现场工人)
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;

    private Integer deleted;

    private String m1;

    private String m2;

    private String m3;

    private String m4;

    private String m5;
}
