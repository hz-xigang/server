package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.LocArchiveDto
import com.gz.xg.domain.search.LocArchiveSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.LocArchiveService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/loc")
class LocArchiveController(
   private  val service: LocArchiveService
) : BaseController()
{

    @RequestMapping("", method = [RequestMethod.POST, RequestMethod.PUT])
    fun save(@RequestBody @Validated dto : LocArchiveDto, request: HttpServletRequest) : ResponseResult {
        if (request.method == RequestMethod.POST.name){
            service.add(dto)
        }else{
            service.updateById(dto)
        }
        return success()
    }

    @DeleteMapping("batch")
    fun dropByIds(@RequestBody ids : List<String>) : ResponseResult{
        service.changeDeleteByIds(ids)
        return success()
    }

    @DeleteMapping("{id}")
    fun dropById(@PathVariable id: String) : ResponseResult{
        service.changeDeleteById(id)
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

    @GetMapping("")
    fun list() : ResponseResult {
        return success(service.list())
    }


}