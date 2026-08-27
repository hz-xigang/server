package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.SysRoleDto
import com.gz.xg.domain.req.BindRoleRightReq
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysRoleService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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

    @OpLog(title = "角色管理", opName = "新增角色", businessType = BusinessType.INSERT)
    @PostMapping("")
    fun add(@RequestBody @Validated dto : SysRoleDto) : ResponseResult{
        return success(service.add(dto))
    }

    @OpLog(title = "角色管理", opName = "修改角色", businessType = BusinessType.UPDATE)
    @PutMapping("")
    fun edit(@RequestBody @Validated dto : SysRoleDto) : ResponseResult{
        service.editById(dto)
        return success()
    }

    @OpLog(title = "角色管理", opName = "角色分配权限", businessType = BusinessType.UPDATE)
    @PostMapping("right")
    fun bindRight(@RequestBody @Validated req : BindRoleRightReq) : ResponseResult{
        service.bindRights(req)
        return success()
    }

    @GetMapping("menu-id/{id}")
    fun getMenuId(@PathVariable id: String) : ResponseResult{
        return success(service.getMenuId(id))
    }

    @OpLog(title = "角色管理", opName = "删除角色", businessType = BusinessType.DELETE)
    @DeleteMapping("{id}")
    fun  softDel(@PathVariable id: String) : ResponseResult{
        service.softDel(id)
        return success()
    }

}