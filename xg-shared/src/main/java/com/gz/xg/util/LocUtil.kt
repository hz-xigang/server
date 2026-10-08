package com.gz.xg.util

class LocUtil {

    companion object {

        /**
         * 按库位 id 升序拼接货位编码。同一货位只保留一次。
         * 入参为 (locId, locCode)。
         */
        fun joinLocCodesById(locs: Collection<Pair<String?, String?>>): String {
            return locs
                .filter { !it.second.isNullOrBlank() }
                .groupBy { it.second }
                .map { (code, items) -> (items.mapNotNull { it.first }.minOrNull() ?: "") to code }
                .sortedBy { it.first }
                .joinToString(",") { it.second ?: "" }
        }
    }
}
