package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.TransferRecordService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transfer")
class TransferRecordController(
    private val service: TransferRecordService
) : BaseController() {

    @PostMapping("")
    @OpLog(title = "调拨作业", opName = "调拨扫码确认", businessType = BusinessType.INSERT)
    fun add(@RequestBody order: AddStockOrder) : ResponseResult{
        service.add(order)
        return success()
    }

}