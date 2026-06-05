package com.gz.xg.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Business sequence rule configuration.
 */
@Data
public class SysSequenceDto {

    /**
     * Global unique primary key ID.
     */
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
}
