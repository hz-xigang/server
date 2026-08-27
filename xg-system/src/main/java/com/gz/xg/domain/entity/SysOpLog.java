package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志表：记录系统功能操作日志，包括模块、操作名称、方法、操作人、请求参数、返回结果、异常信息及执行耗时等
 */
@Data
@TableName("SysOpLog")
public class SysOpLog {

    /**
     * 主键ID，生成唯一字符串
     */
    @TableId
    private String id;

    /**
     * 模块或业务标题，例如：文件模板、库位档案
     */
    private String title;

    /**
     * 具体操作名称，例如：新增库位、修改密码、用户登录、批量删除
     */
    private String opName;

    /**
     * 业务类型：0-其他，1-新增，2-修改，3-删除，4-上传，5-查询
     */
    private Integer businessType;

    /**
     * 执行的方法全类名及方法名
     */
    private String method;

    /**
     * HTTP请求方式，例如：GET、POST、PUT、DELETE
     */
    private String requestMethod;

    /**
     * 操作人员ID
     */
    private String operUserId;

    /**
     * 操作人员账号，即登录用户名
     */
    private String operName;

    /**
     * 操作人员姓名
     */
    private String operRealName;

    /**
     * 请求URL地址
     */
    private String operUrl;

    /**
     * 操作人员请求IP地址
     */
    private String operIp;

    /**
     * 请求参数，JSON格式
     */
    private String operParam;

    /**
     * 请求返回结果，JSON格式
     */
    private String jsonResult;

    /**
     * 操作状态：0-成功，1-失败
     */
    private Integer status;

    /**
     * 操作失败时的错误简要信息
     */
    private String errorMsg;

    /**
     * 异常完整堆栈信息
     */
    private String stackTrace;

    /**
     * 操作执行耗时，单位：毫秒
     */
    private Long costTime;

    /**
     * 操作时间，默认取当前系统时间
     */
    private LocalDateTime operTime;
}
