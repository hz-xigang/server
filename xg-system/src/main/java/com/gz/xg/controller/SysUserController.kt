package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.SysUserDto
import com.gz.xg.domain.req.BindUserRoleReq
import com.gz.xg.domain.req.LoginReq
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysUserService
import jakarta.annotation.Resource
import jakarta.servlet.http.HttpServletRequest
import org.springframework.validation.annotation.Validated
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

    @PostMapping("login")
    fun login(@RequestBody @Validated loginReq: LoginReq): ResponseResult {
        return success(service.login(loginReq))
    }

    @PostMapping("page")
    fun page(
        @RequestParam(value = "page", defaultValue = "1") page: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
        @RequestParam(value = "deleted", required = false) deleted: Int?
    ) : ResponseResult
    {
        return success(service.page(page,size,deleted))
    }

    @PostMapping
    fun add(@Validated @RequestBody dto: SysUserDto): ResponseResult {
        service.add(dto)
        return success()
    }

    @PutMapping
    fun edit(@Validated @RequestBody dto: SysUserDto): ResponseResult {
        service.edit(dto)
        return success()
    }

    @PostMapping("bind-role")
    fun bindRole(@RequestBody @Validated req : BindUserRoleReq) : ResponseResult {
        service.bindRole(req)
        return success()
    }

    @GetMapping("role-id/{id}")
    fun getRoleId(@PathVariable id: String) : ResponseResult {
        return success(service.getRoleId(id))
    }


}
