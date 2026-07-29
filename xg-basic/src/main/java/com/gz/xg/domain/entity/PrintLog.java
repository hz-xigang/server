package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("PrintLog")
@Data
public class PrintLog {

    @TableId
    private String  id;

    private String no;

    private Integer type;

    private String tempId;

    private LocalDateTime createTime;

    private String username;

    private String userId;

    private String realName;


}
