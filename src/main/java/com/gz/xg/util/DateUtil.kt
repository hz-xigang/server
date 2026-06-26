package com.gz.xg.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 日期工具类。
 * 提供字符串日期解析与日期偏移计算能力。
 */
class DateUtil {

    companion object {
        /**
         * 将字符串按 `yyyy-MM-dd` 格式解析为日期。
         *
         * @param str 日期字符串。
         * @return 解析后的 LocalDate 对象。
         */
        fun str2LocalDateTime(str: String): LocalDate {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return LocalDate.parse(str, formatter)
        }

        /**
         * 在输入日期上增加指定天数，并按 `yyyy-MM-dd` 返回。
         *
         * @param str 日期字符串。
         * @param days 要增加的天数。
         * @return 增加天数后的日期字符串。
         */
        fun strAddDays(str: String, days: Long = 1): String {
            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateTime = parseToLocalDateTime(str)
            return dateTime.plusDays(days).format(outputFormatter)
        }

        /**
         * 将多种日期时间格式解析为 LocalDateTime。
         * 支持 `yyyy-MM-dd HH:mm:ss`、`yyyy-MM-dd HH:mm`、`yyyy-MM-dd`。
         *
         * @param str 日期时间字符串。
         * @return 解析后的 LocalDateTime 对象。
         */
        private fun parseToLocalDateTime(str: String): LocalDateTime {
            val cleanStr = str.trim()
            val formatters = listOf(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )

            return formatters.asSequence()
                .mapNotNull {
                    try {
                        LocalDateTime.parse(cleanStr, it)
                    } catch (e: Exception) {
                        null
                    }
                }
                .firstOrNull()
                ?: try {
                    LocalDate.parse(cleanStr).atStartOfDay()
                } catch (e: Exception) {
                    throw IllegalArgumentException("不支持的时间格式: $str")
                }
        }
    }
}
