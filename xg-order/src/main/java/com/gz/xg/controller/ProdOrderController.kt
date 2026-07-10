package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.search.ProdOrderSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProdOrderService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prod")
open class ProdOrderController(
    private val service: ProdOrderService
) : BaseController() {

    @GetMapping(value = ["/pgNo/{pgNo}"])
    fun findByProdNo(@PathVariable pgNo: String) : ResponseResult {
        return success(service.findByProgNo(pgNo))
    }

    @PostMapping(value = ["/page"])
    fun page(
        @RequestParam(value = "page", defaultValue = "1") page: Long,
        @RequestParam(value = "size", defaultValue = "1") size: Long,
        @RequestBody search: ProdOrderSearch
    ) : ResponseResult
    {
        return success(service.page(page,size,search))
    }





}