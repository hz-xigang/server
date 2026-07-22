package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysUserRole
import com.gz.xg.mapper.SysUserRoleMapper
import org.springframework.stereotype.Service

@Service
class SysUserRolePlusService : ServiceImpl<SysUserRoleMapper, SysUserRole>() {

    fun dropUserRole(userId : String){
        val wrapper = LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        this.remove(wrapper)

    }

}