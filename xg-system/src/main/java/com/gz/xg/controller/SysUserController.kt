package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.LoginReq
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysUserService
import jakarta.annotation.Resource
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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


}
