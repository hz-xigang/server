package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

data class BindRoleRightReq(

    @field:NotBlank(message = "角色不能为空")
    val roleId : String,

    val menuIds: List<String>,


)