package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.FileTempDto
import com.gz.xg.domain.search.FileTempSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.FileTempService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("api/file-temp")
class FileTempController(
    private val service: FileTempService
) : BaseController() {

    @RequestMapping("", method = [RequestMethod.POST, RequestMethod.PUT])
    @OpLog(title = "文件模板", opName = "保存模板", businessType = BusinessType.OTHER)
    fun save(@RequestBody dto: FileTempDto, request: HttpServletRequest): ResponseResult {
        if (request.method == RequestMethod.POST.name) {
            service.add(dto)
        } else {
            service.updateById(dto)
        }
        return success()
    }

    @DeleteMapping("batch")
    @OpLog(title = "文件模板", opName = "批量删除模板", businessType = BusinessType.DELETE)
    fun dropByIds(@RequestBody ids: List<String>): ResponseResult {
        service.changeDeleteByIds(ids)
        return success()
    }

    @DeleteMapping("{id}")
    @OpLog(title = "文件模板", opName = "删除模板", businessType = BusinessType.DELETE)
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

    @PostMapping("{id}/upload")
    @OpLog(title = "文件模板", opName = "上传模板文件", businessType = BusinessType.UPLOAD)
    fun uploadFile(
        @PathVariable id: String,
        @RequestParam("file") file: MultipartFile
    ): ResponseResult {
        service.uploadFile(id, file)
        return success()
    }
}
