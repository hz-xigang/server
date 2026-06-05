package com.gz.xg.controller

import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProductionOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prod")
open class ProdOrderController(
    private val service: ProductionOrderService
) : BaseController() {

    @GetMapping("/pgNo/{pgNo}")
    fun findByProgNo(@PathVariable pgNo: String) : ResponseResult {
        return success(service.findByProgNo(pgNo))
    }


}