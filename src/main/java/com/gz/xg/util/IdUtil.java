package com.gz.xg.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class IdUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * ID格式：
     * yyyyMMddHHmmssSSS + 8位随机数
     */
    public static String generateId() {

        String time = LocalDateTime.now().format(FORMATTER);

        // 8位随机数：10000000 ~ 99999999
        int random = ThreadLocalRandom.current()
                .nextInt(10_000_000, 100_000_000);

        return time + random;
    }

}
