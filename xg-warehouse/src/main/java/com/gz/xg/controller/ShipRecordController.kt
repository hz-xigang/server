package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ShipRecordService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ship")
class ShipRecordController(
    private val shipRecordService: ShipRecordService,
) : BaseController(){

    @PostMapping("")
    fun add(@RequestBody order: AddStockOrder) : ResponseResult{
        shipRecordService.add(order)
        return success()
    }

}