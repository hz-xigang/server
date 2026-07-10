package com.gz.xg.domain.auth

data class LoginUser(
    val userId: String,
    val username: String,
    val realName: String?,
    val type: Int?
)