package com.gz.xg.controller

import com.gz.xg.domain.dto.LocArchiveDto
import com.gz.xg.domain.search.LocArchiveSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.LocArchiveService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/loc")
class LocArchiveController(
   private  val service: LocArchiveService
) : BaseController()
{

    @PostMapping("")
    fun add(@RequestBody @Validated dto : LocArchiveDto) : ResponseResult{
        service.add(dto)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestBody  search: LocArchiveSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
    ) : ResponseResult
    {
        return success(
            service.page(current, size,search)
        )
    }


}