package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统角色表
 */
@Data
public class SysRoleDto {

    private String id;

    /**
     * 角色名称(如：仓库主管、现场工人)
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 备注
     */
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private String m1;

    private String m2;

    private String m3;

    private String m4;

    private String m5;
}
