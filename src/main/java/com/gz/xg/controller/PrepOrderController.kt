package com.gz.xg.controller

import com.gz.xg.domain.dto.PrepOrderDto
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PrepOrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @module 备料单
 */
@RestController
@RequestMapping("api/prep/order")
class PrepOrderController(
    private val service: PrepOrderService
) : BaseController() {


    @PostMapping("")
    fun add(@RequestBody prepOrderDto: PrepOrderDto) : ResponseResult{
        service.add(prepOrderDto)
        return success()
    }

    /**
     * 分页读取
     */
    @PostMapping("page")
    fun page(
        @RequestParam("page") current: Long,
        @RequestParam("size") size: Long,
    ) : ResponseResult{
        
        return success(service.page(current,size))
    }

}