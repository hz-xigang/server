package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.SysRoleDto
import com.gz.xg.domain.req.BindRoleRightReq
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysRoleService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/role")
class SysRoleController(
    private val service : SysRoleService
) : BaseController()
{

    @GetMapping("")
    fun list() : ResponseResult{
        return success(service.list())
    }

    @RequestMapping("",method = [RequestMethod.POST, RequestMethod.PUT])
    fun add(@RequestBody @Validated dto : SysRoleDto,request: HttpServletRequest) : ResponseResult{

        if (request.method.uppercase() == RequestMethod.POST.name){
            service.add(dto)
        }else{
            service.editById(dto)
        }

        return success()
    }

    @PostMapping("right")
    fun bindRight(@RequestBody @Validated req : BindRoleRightReq) : ResponseResult{
        service.bindRights(req)
        return success()
    }

    @GetMapping("menu-id/{id}")
    fun getMenuId(@PathVariable id: String) : ResponseResult{
        return success(service.getMenuId(id))
    }

    @DeleteMapping("{id}")
    fun  softDel(@PathVariable id: String) : ResponseResult{
        service.softDel(id)
        return success()
    }

}