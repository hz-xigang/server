package com.gz.xg.config.resource

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 静态资源路径配置类。
 * 读取 `static` 前缀下的目录配置。
 */
@Component
@ConfigurationProperties(prefix = "static")
class StaticYmlConfig {
    /**
     * 模板目录子路径。
     */
    var template: String? = null

    /**
     * 临时目录子路径。
     */
    var tmp: String? = null

    /**
     * 静态资源根路径。
     */
    var base: String? = null

    /**
     * 上传目录子路径。
     */
    var upload: String? = null

    /**
     * 获取模板目录完整路径。
     *
     * @return 根路径与模板子路径拼接后的完整路径。
     */
    fun fullTemplate(): String {
        return base!! + template!!
    }

    /**
     * 获取临时目录完整路径。
     *
     * @return 根路径与临时子路径拼接后的完整路径。
     */
    fun fullTmp(): String {
        return base!! + tmp!!
    }
}
