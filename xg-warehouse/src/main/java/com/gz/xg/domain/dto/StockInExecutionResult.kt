package com.gz.xg.domain.dto

/**
 * 入库整体执行结果
 */
data class StockInExecutionResult(
    val successCount: Int,
    val failTags: List<StockInFailTag>
)
