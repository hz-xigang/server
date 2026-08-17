package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.search.PrintLogSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PrintLogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/print-log")
class PrintLogController(
    private val service: PrintLogService
) : BaseController() {

    @PostMapping("page")
    fun page(
        @RequestBody search: PrintLogSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ): ResponseResult {
        return success(service.page(current, size, search))
    }

    @PostMapping("page/carton-label")
    fun pageCartonLabel(
        @RequestBody search: PrintLogSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ): ResponseResult {
        return success(service.pageCartonLabel(current, size, search))
    }

    @PostMapping("page/pallet-label")
    fun pagePalletLabel(
        @RequestBody search: PrintLogSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ): ResponseResult {
        return success(service.pagePalletLabel(current, size, search))
    }
}
