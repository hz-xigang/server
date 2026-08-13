package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class ChangePwdReq {

    @NotBlank(message = "原密码不能为空")
    var oldPwd: String? = null

    @NotBlank(message = "新密码不能为空")
    var newPwd: String? = null
}
