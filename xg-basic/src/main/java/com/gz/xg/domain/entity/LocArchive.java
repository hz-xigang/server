package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 仓库物理库位档案配置表
 */
@Data
@TableName("locArchive")
public class LocArchive  {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * 库位/货位唯一编码(如: A-02-05, Z01)
     */
    private String locCode;

    /**
     * 库位区域类型(如: 收料区、货架区、发货区、打包区)
     */
    private String locType;

    /**
     * 库位当前占用状态(空闲/占用/禁用)
     */
    private String status;

    /**
     * 库位档案创建录入时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常, 1:已删除)
     */
    private Integer deleted;

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
