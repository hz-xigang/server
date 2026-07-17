package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.SysRightService
import org.springframework.web.bind.annotation.GetMapping
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



}