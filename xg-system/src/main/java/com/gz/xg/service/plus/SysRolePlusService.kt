package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysRole
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.SysRoleMapper
import org.springframework.stereotype.Service

@Service
class SysRolePlusService : ServiceImpl<SysRoleMapper, SysRole>(){

    fun byId(id: String): SysRole {
        return this.getById(id) ?: throw WebException("该角色不存在")
    }

    fun byName(name: String,roleId : String?) : SysRole? {
        val wrapper = LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getName, name)
            .ne(!roleId.isNullOrBlank(), SysRole::getId,roleId)
        return this.getOne(wrapper)
    }



}
