package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PrepRecordService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/prep")
class PrepRecordController(
    private val prepRecordService: PrepRecordService
) : BaseController()
{
    @PostMapping("")
    fun add(@RequestBody order: AddStockOrder) : ResponseResult{
        prepRecordService.add(order)
        return success()
    }
}