package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.OrderTodoService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * PDA 移动端指令单待办提醒汇总控制器
 */
@RestController
@RequestMapping("api/order")
class OrderTodoController(
    private val service: OrderTodoService
) : BaseController() {

    /**
     * 查询发货指令单、备料指令单、调拨指令单的待处理汇总及最新单据标识
     */
    @GetMapping("todo-summary")
    fun todoSummary(): ResponseResult {
        return success(service.getTodoSummary())
    }
}
