package com.gz.xg.domain.resp

data class LoginResp(
    val token: String,
    val tokenType: String,
    val expiresAt: Long,
    val expiresIn: Long,
    val userId: String,
    val username: String,
    val realName: String?,
    val type: Int?
)
