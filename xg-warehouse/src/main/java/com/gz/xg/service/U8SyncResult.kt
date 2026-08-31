package com.gz.xg.service

/**
 * U8 同步结果。
 *
 * @param statusMap 同步状态映射（入库按 prodOrderId，移库/出库按 tagNo）：0-未同步，1-已同步，2-不需同步
 * @param failCount 同步失败数量
 * @param errorMessage U8 返回的错误信息（有失败时非空，透传给前端展示）
 */
data class U8SyncResult(
    val statusMap: Map<String, Int> = emptyMap(),
    val failCount: Int = 0,
    val errorMessage: String? = null
)
