package com.gz.xg.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 仓库物理库位档案配置表
 */
@Data
public class LocArchiveDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 库位/货位唯一编码(如: A-02-05, Z01)
     */
    @NotBlank(message = "编码不能为空")
    private String locCode;

    /**
     * 库位区域类型(如: 收料区、货架区、发货区、打包区)
     */
    @NotBlank(message = "类型不能为空")
    private String locType;

    /**
     * 库位当前占用状态(空闲/占用/禁用)
     */
    private String status;

    /**
     * 库位档案创建录入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
