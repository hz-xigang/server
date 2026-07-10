package com.gz.xg.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Business sequence rule configuration.
 */
@Getter
@Setter
@TableName("sysSequence")
public class SysSequence {

    /**
     * Global unique primary key ID.
     */
    @TableId
    private String id;

    /**
     * Business type identifier.
     */
    private Integer bizType;

    /**
     * Prefix characters, for example SC in SC2026.
     */
    private String prefix;

    /**
     * Padding length of the sequence number part.
     */
    private Integer seqLength;

    /**
     * Current maximum used counter value.
     */
    private Integer currentValue;

    /**
     * Enabled status, true for enabled and false for disabled.
     */
    private Boolean status;

    /**
     * Configuration creation time.
     */
    private LocalDateTime createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getSeqLength() {
        return seqLength;
    }

    public void setSeqLength(Integer seqLength) {
        this.seqLength = seqLength;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
