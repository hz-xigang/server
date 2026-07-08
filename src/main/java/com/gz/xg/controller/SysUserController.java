package com.gz.xg.controller;

import com.gz.xg.domain.req.LoginReq;
import com.gz.xg.exception.ResponseResult;
import com.gz.xg.service.SysUserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class SysUserController extends BaseController{

    @Resource
    private SysUserService service;

    @PostMapping("login")
    public ResponseResult login(@RequestBody @Validated LoginReq loginReq) {
        return success(service.login(loginReq));
    }



}
