package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.SysUserDto
import com.gz.xg.domain.req.BindUserRoleReq
import com.gz.xg.domain.req.ChangePwdReq
import com.gz.xg.domain.req.LoginReq
import com.gz.xg.domain.req.UserSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysUserService
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/user")
class SysUserController(
    private val service: SysUserService
) : BaseController() {

    @OpLog(title = "用户中心", opName = "用户登录", businessType = BusinessType.OTHER)
    @PostMapping("login")
    fun login(@RequestBody @Validated loginReq: LoginReq): ResponseResult {
        return success(service.login(loginReq))
    }

    @OpLog(title = "用户中心", opName = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("change-pwd")
    fun changePwd(@RequestBody @Validated req: ChangePwdReq): ResponseResult {
        service.changePwd(req)
        return success()
    }

    @PostMapping("page")
    fun page(
        @RequestParam(value = "page", defaultValue = "1") page: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
        @RequestBody search: UserSearch
    ) : ResponseResult
    {
        return success(
            service.page(page,size,search)
        )
    }

    @OpLog(title = "用户管理", opName = "新增用户", businessType = BusinessType.INSERT)
    @PostMapping
    fun add(@Validated @RequestBody dto: SysUserDto): ResponseResult {
        service.add(dto)
        return success()
    }

    @OpLog(title = "用户管理", opName = "修改用户", businessType = BusinessType.UPDATE)
    @PutMapping
    fun edit(@Validated @RequestBody dto: SysUserDto): ResponseResult {
        service.edit(dto)
        return success()
    }

    @OpLog(title = "用户管理", opName = "用户分配角色", businessType = BusinessType.UPDATE)
    @PostMapping("bind-role")
    fun bindRole(@RequestBody @Validated req : BindUserRoleReq) : ResponseResult {
        service.bindRole(req)
        return success()
    }

    @GetMapping("role-id/{id}")
    fun getRoleId(@PathVariable id: String) : ResponseResult {
        return success(service.getRoleId(id))
    }

    @OpLog(title = "用户管理", opName = "删除用户", businessType = BusinessType.DELETE)
    @DeleteMapping("{id}")
    fun softDel(@PathVariable id: String) : ResponseResult{
        service.softDel(id)
        return success()
    }


}
