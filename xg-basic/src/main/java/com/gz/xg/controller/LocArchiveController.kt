package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.LocArchiveDto
import com.gz.xg.domain.search.LocArchiveSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.LocArchiveService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/loc")
class LocArchiveController(
    private val service: LocArchiveService
) : BaseController() {

    @RequestMapping("", method = [RequestMethod.POST, RequestMethod.PUT])
    @OpLog(title = "库位档案", opName = "保存库位", businessType = BusinessType.OTHER)
    fun save(@RequestBody @Validated dto: LocArchiveDto, request: HttpServletRequest): ResponseResult {
        if (request.method == RequestMethod.POST.name) {
            service.add(dto)
        } else {
            service.updateById(dto)
        }
        return success()
    }

    @DeleteMapping("batch")
    @OpLog(title = "库位档案", opName = "批量删除库位", businessType = BusinessType.DELETE)
    fun dropByIds(@RequestBody ids: List<String>): ResponseResult {
        service.changeDeleteByIds(ids)
        return success()
    }

    @DeleteMapping("{id}")
    @OpLog(title = "库位档案", opName = "删除库位", businessType = BusinessType.DELETE)
    fun dropById(@PathVariable id: String): ResponseResult {
        service.changeDeleteById(id)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: LocArchiveSearch,
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
