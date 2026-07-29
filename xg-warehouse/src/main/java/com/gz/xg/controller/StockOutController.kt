package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockOutService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/stock/out")
class StockOutController(
    private val service: StockOutService,
)  : BaseController()
{

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