package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.search.PrintLogSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PrintLogService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/print-log")
class PrintLogController(
    private val service: PrintLogService
) : BaseController() {

    @DeleteMapping("{id}")
    @OpLog(title = "打印日志", businessType = BusinessType.DELETE)
    fun dropById(@PathVariable id: String): ResponseResult {
        service.changeDeleteById(id)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: PrintLogSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ): ResponseResult {
        return success(
            service.page(current, size, search)
        )
    }

    @GetMapping("")
    fun list(): ResponseResult {
        return success(service.list())
    }
}
