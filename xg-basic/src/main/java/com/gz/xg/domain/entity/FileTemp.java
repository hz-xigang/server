package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文件模板
 */
@Data
@TableName("FileTemp")
public class FileTemp {

    @TableId
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 宽
     */
    private BigDecimal width;

    /**
     * 高
     */
    private BigDecimal height;

    /**
     * 1-纸箱标签
     */
    private Integer type;

    private Integer deleted;

    private LocalDateTime createTime;

    private String userId;

    private String realName;

    /**
     * 文件路径
     */
    private String path;

}
