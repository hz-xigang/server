package com.gz.xg.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadLocalRandom

object IdUtil {
    private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")

    /**
     * ID格式：
     * yyyyMMddHHmmssSSS + 8位随机数
     */
    fun generateId(): String {
        val time: String = LocalDateTime.now().format(FORMATTER)

        // 8位随机数：10000000 ~ 99999999
        val random: Int = ThreadLocalRandom.current()
            .nextInt(10000000, 100000000)

        return time + random
    }
}
