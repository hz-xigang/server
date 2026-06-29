package com.gz.xg.controller

import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockMoveService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/stock/move")
class StockMoveController(
    private val service: StockMoveService
) : BaseController(){

    @PostMapping("")
    fun add (@RequestBody req : AddStockIn) : ResponseResult{
        service.add(req)
        return success()
    }

}