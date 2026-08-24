package com.gz.xg.u8.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用友 U8 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "u8")
public class U8Config {

    /**
     * 用友接口基础 URL
     */
    private String url;

    /**
     * 账套编号（默认值）
     */
    private String caccId = "108";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 5000;

    /**
     * 读取超时时间（毫秒）
     */
    private Integer readTimeout = 10000;
}
