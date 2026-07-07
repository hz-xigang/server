package com.gz.xg.controller

import com.gz.xg.domain.search.ShipOrderSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ShipOrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/ship/order")
class ShipOrderController(private val service: ShipOrderService) : BaseController()
{

    @PostMapping("page")
    fun page(
        @RequestParam("page") current: Long,
        @RequestParam("size") size: Long,
        @RequestBody search: ShipOrderSearch
    ) : ResponseResult
    {
        return success(service.page(current,size,search))
    }

}