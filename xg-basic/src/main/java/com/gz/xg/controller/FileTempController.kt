package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.FileTempDto
import com.gz.xg.domain.search.FileTempSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.FileTempService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("api/file-temp")
class FileTempController(
    private val service: FileTempService
) : BaseController() {

    @RequestMapping("", method = [RequestMethod.POST, RequestMethod.PUT])
    fun save(@RequestBody dto: FileTempDto, request: HttpServletRequest): ResponseResult {
        if (request.method == RequestMethod.POST.name) {
            service.add(dto)
        } else {
            service.updateById(dto)
        }
        return success()
    }

    @DeleteMapping("batch")
    fun dropByIds(@RequestBody ids: List<String>): ResponseResult {
        service.changeDeleteByIds(ids)
        return success()
    }

    @DeleteMapping("{id}")
    fun dropById(@PathVariable id: String): ResponseResult {
        service.changeDeleteById(id)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: FileTempSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ): ResponseResult {
        return success(service.page(current, size, search))
    }

    @GetMapping("")
    fun list(): ResponseResult {
        return success(service.list())
    }
}
