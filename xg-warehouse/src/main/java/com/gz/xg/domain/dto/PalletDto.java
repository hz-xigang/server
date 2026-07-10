package com.gz.xg.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打托单据主表(托盘档案表)
 */
@Data
public class PalletDto {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    private String id;

    /**
     * 系统唯一托盘号/大箱号(格式如TP-xxxx)
     */
    private String palletNo;

    /**
     * 该托盘内包含的纸箱总件数
     */
    private Integer qty;



    /**
     * 打托绑定/标签打印时间
     */
    private LocalDateTime createTime;

    /**
     * 自定义扩展备注字段
     */
    private String m1;

    /**
     * 自定义扩展备注字段
     */
    private String m2;

    /**
     * 自定义扩展备注字段
     */
    private String m3;

    /**
     * 自定义扩展备注字段
     */
    private String m4;

    /**
     * 自定义扩展备注字段
     */
    private String m5;

    private String username;

    private String userId;

}
