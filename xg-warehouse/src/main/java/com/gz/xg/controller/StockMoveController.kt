package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockMoveService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/stock/move")
class StockMoveController(
    private val service: StockMoveService
) : BaseController(){

    @PostMapping("")
    @OpLog(title = "移库管理", opName = "库位移库", businessType = BusinessType.INSERT)
    fun add (@RequestBody req : AddStockIn) : ResponseResult{
        service.add(req)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: StockSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ) : ResponseResult
    {
        return success(
            service.page(search, current, size)
        )
    }



}