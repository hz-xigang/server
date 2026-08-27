package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 */
@Data
@TableName("SysOpLog")
public class SysOpLog {

    @TableId
    private String id;

    private String title;

    private Integer businessType;

    private String method;

    private String requestMethod;

    private String operName;

    private String operRealName;

    private String operUserId;

    private String operUrl;

    private String operIp;

    private String operParam;

    private String jsonResult;

    private Integer status;

    private String errorMsg;

    private String stackTrace;

    private Long costTime;

    private LocalDateTime operTime;
}
