package com.gz.xg.util

class LocUtil {

    companion object {

        fun joinLocCodes(locCodes: Collection<String?>, head: String? = null): String {
            val rest = locCodes.filterNotNull()
                .filter { it.isNotBlank() && it != head }
                .distinct()
                .sorted()
            return if (head.isNullOrBlank()) {
                rest.joinToString(",")
            } else {
                (listOf(head) + rest).joinToString(",")
            }
        }
    }
}
