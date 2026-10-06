package com.gz.xg.controller

import com.gz.xg.UserContext
import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysRightService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/right")
class SysRightController(
    private val service: SysRightService
) : BaseController() {

    @GetMapping("/menu/tree")
    fun menuTree() : ResponseResult {
        return success(service.menuTree())
    }

    @GetMapping("/menu/tree/current")
    fun currentUserMenuTree() : ResponseResult {
        val loginUser = UserContext.require()
        return success(service.menuTreeByUserId(loginUser.userId))
    }

    @GetMapping("/pda/list")
    fun pdaList() : ResponseResult {
        return success(service.pdaList())
    }

    @GetMapping("/pda/list/current")
    fun currentUserPdaList() : ResponseResult {
        val loginUser = UserContext.require()
        return success(service.pdaListByUserId(loginUser.userId))
    }

    @GetMapping("/pda/list/role/{roleId}")
    fun pdaListByRoleId(@PathVariable roleId: String) : ResponseResult {
        return success(service.pdaListByRoleId(roleId))
    }

}
