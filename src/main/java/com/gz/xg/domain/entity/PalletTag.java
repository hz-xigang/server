package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("palletTag")
public class PalletTag {

    private String pId;
    private String tagNo;

}
