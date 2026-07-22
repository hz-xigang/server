package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysUser
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.SysUserMapper
import org.springframework.stereotype.Service

@Service
class SysUserPlusService : ServiceImpl<SysUserMapper, SysUser>(){

    fun findByUsername(username: String?): SysUser {
        val wrapper = LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username)

        return this.getOne(wrapper) ?: throw WebException("用户或密码错误")
    }

    fun byId(id: String): SysUser {
        return this.getById(id) ?: throw WebException("用户不存在")
    }


}