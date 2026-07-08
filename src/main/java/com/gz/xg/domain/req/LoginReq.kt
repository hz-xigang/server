package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class LoginReq {

    @NotBlank(message = "用户名和密码不能为空")
    var username: String? = null

    @NotBlank(message = "用户名和密码不能为空")
    var pwd: String? = null

}