package com.gz.xg.controller

import com.gz.xg.domain.search.TransferOrderSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.TransferOrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/transfer/order")
class TransferOrderController(private val service: TransferOrderService) : BaseController() {

    @PostMapping("page")
    fun page(
        @RequestParam("page") current: Long,
        @RequestParam("size") size: Long,
        @RequestBody search: TransferOrderSearch
    ) : ResponseResult
    {
        return success(service.page(current,size,search))
    }

}