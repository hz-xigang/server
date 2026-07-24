package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 打托单据主表(托盘档案表)
 */
@Data
@TableName("pallet")
public class Pallet {

    /**
     * 全局唯一主键ID(字符串类型)
     */
    @TableId
    private String id;

    /**
     * 系统唯一托盘号/大箱号(格式如TP-xxxx)
     */
    private String palletNo;

    /**
     * 该托盘内包含的纸箱总件数
     */
    private Integer qty;

    private BigDecimal grossWeight;

    private BigDecimal netWeight;



    /**
     * 打托绑定/标签打印时间
     */
    private LocalDateTime createTime;

    /**
     * 软删除逻辑标记(0:正常, 1:已删除)
     */
    private Integer deleted;

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

    private String realName;

}
