package com.gz.xg.service

import com.gz.xg.domain.dto.SysUserDto
import com.gz.xg.domain.mapstruct.SysUserMapStruct
import com.gz.xg.domain.req.LoginReq
import com.gz.xg.domain.resp.LoginResp
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.SysUserPlusService
import com.gz.xg.util.IdUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SysUserService(
    private val plusService: SysUserPlusService,
    private val passwordEncoder : PasswordEncoder,
    private val sysUserMapStruct: SysUserMapStruct,
    private val jwtService: JwtService
) {

    fun add(dto: SysUserDto){
        val user = sysUserMapStruct.toEntity(dto)
        user.pwd = passwordEncoder.encode(user.pwd)
        user.id = IdUtil.generateId()
        plusService.save(user)
    }

    fun login(req : LoginReq): String {
        val user = plusService.findByUsername(req.username)

        val matches = passwordEncoder.matches(req.pwd, user.pwd)
        if (!matches) {
             throw WebException("用户或密码错误")
        }

        return jwtService.generateToken(user)
    }

}
