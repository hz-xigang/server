package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.OrderTodoItemDto
import com.gz.xg.domain.dto.OrderTodoSummaryDto
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.service.plus.PrepOrderPlusService
import com.gz.xg.service.plus.ShipOrderPlusService
import com.gz.xg.service.plus.TransferOrderPlusService
import org.springframework.stereotype.Service

/**
 * PDA 移动端指令单待办提醒汇总服务
 */
@Service
class OrderTodoService(
    private val shipOrderPlusService: ShipOrderPlusService,
    private val prepOrderPlusService: PrepOrderPlusService,
    private val transferOrderPlusService: TransferOrderPlusService
) : BaseService() {

    /**
     * 获取发货指令单、备料指令单、调拨指令单的待处理数量及最新单据信息
     */
    fun getTodoSummary(): OrderTodoSummaryDto {
        val shipTodo = queryShipOrderTodo()
        val prepTodo = queryPrepOrderTodo()
        val transferTodo = queryTransferOrderTodo()

        return OrderTodoSummaryDto(
            totalPendingCount = shipTodo.pendingCount + prepTodo.pendingCount + transferTodo.pendingCount,
            shipOrder = shipTodo,
            prepOrder = prepTodo,
            transferOrder = transferTodo
        )
    }

    private fun queryShipOrderTodo(): OrderTodoItemDto {
        val wrapper = LambdaQueryWrapper<ShipOrder>()
            .select(ShipOrder::getId, ShipOrder::getShipNo, ShipOrder::getCreateTime)
            .eq(ShipOrder::getStatus, 0)
            .and { it.eq(ShipOrder::getDeleted, 0).or().isNull(ShipOrder::getDeleted) }
            .orderByDesc(ShipOrder::getId)

        val pageObj = shipOrderPlusService.page(Page(1, 1), wrapper)
        val latest = pageObj.records.firstOrNull()
        return OrderTodoItemDto(
            pendingCount = pageObj.total,
            latestId = latest?.id,
            latestNo = latest?.shipNo,
            latestCreateTime = latest?.createTime
        )
    }

    private fun queryPrepOrderTodo(): OrderTodoItemDto {
        val wrapper = LambdaQueryWrapper<PrepOrder>()
            .select(PrepOrder::getId, PrepOrder::getPrepNo, PrepOrder::getCreateTime)
            .`in`(PrepOrder::getStatus, listOf(0, 1))
            .and { it.eq(PrepOrder::getDeleted, 0).or().isNull(PrepOrder::getDeleted) }
            .orderByDesc(PrepOrder::getId)

        val pageObj = prepOrderPlusService.page(Page(1, 1), wrapper)
        val latest = pageObj.records.firstOrNull()
        return OrderTodoItemDto(
            pendingCount = pageObj.total,
            latestId = latest?.id,
            latestNo = latest?.prepNo,
            latestCreateTime = latest?.createTime
        )
    }

    private fun queryTransferOrderTodo(): OrderTodoItemDto {
        val wrapper = LambdaQueryWrapper<TransferOrder>()
            .select(TransferOrder::getId, TransferOrder::getOrderNo, TransferOrder::getCreateTime)
            .eq(TransferOrder::getStatus, 0)
            .and { it.eq(TransferOrder::getDeleted, 0).or().isNull(TransferOrder::getDeleted) }
            .orderByDesc(TransferOrder::getId)

        val pageObj = transferOrderPlusService.page(Page(1, 1), wrapper)
        val latest = pageObj.records.firstOrNull()
        return OrderTodoItemDto(
            pendingCount = pageObj.total,
            latestId = latest?.id,
            latestNo = latest?.orderNo,
            latestCreateTime = latest?.createTime
        )
    }
}
