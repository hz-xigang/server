package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("palletTag")
public class PalletTag extends TagEntity {

    /**
     * 软删除逻辑标记(0:正常, 1:已删除)
     */
    private Integer deleted;

}
