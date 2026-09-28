package com.gz.xg.domain.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

/**
 * PDA 指令单待办提醒汇总响应结构
 */
data class OrderTodoSummaryDto(
    /**
     * 三类待处理指令单总数量
     */
    val totalPendingCount: Long,

    /**
     * 发货指令单待办汇总
     */
    val shipOrder: OrderTodoItemDto,

    /**
     * 备料指令单待办汇总
     */
    val prepOrder: OrderTodoItemDto,

    /**
     * 调拨指令单待办汇总
     */
    val transferOrder: OrderTodoItemDto
)

/**
 * 单类指令单待办提醒明细
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
data class OrderTodoItemDto(
    /**
     * 待处理单据数量
     */
    val pendingCount: Long,

    /**
     * 最新一条待处理单据的主键ID（无待办时为 null）
     */
    val latestId: String? = null,

    /**
     * 最新一条待处理单据的指令单号（无待办时为 null）
     */
    val latestNo: String? = null,

    /**
     * 最新一条待处理单据的创建时间（无待办时为 null）
     */
    @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    val latestCreateTime: LocalDateTime? = null
)
