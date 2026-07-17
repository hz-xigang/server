package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockInSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockInService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/stock/in")
class StockInController(
    private val service: StockInService
) : BaseController()
{
    @PostMapping("")
    fun add (@RequestBody req : AddStockIn,
             @RequestParam(defaultValue = "0" )type : Int) : ResponseResult{

        if (type == 1){
            service.addReturn(req)
        }else{
            service.add(req)
        }


        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: StockInSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
        ) : ResponseResult
    {
        return success(
            service.page(search, current, size)
        )
    }


}