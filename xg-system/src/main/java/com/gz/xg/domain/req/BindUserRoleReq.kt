package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

data class BindUserRoleReq(

    @field:NotBlank(message = "用户不能为空")
    val userId : String,

    val roleIds: List<String>,


)