package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.SysRoleRight
import com.gz.xg.domain.req.BindRoleRightReq
import com.gz.xg.mapper.SysRoleRightMapper
import org.springframework.stereotype.Service

@Service
class SysRoleRightPlusService: ServiceImpl<SysRoleRightMapper, SysRoleRight>(){

    fun dropRoleMenu(roleId : String){
        val wrapper = LambdaQueryWrapper<SysRoleRight>()
            .eq(SysRoleRight::getRoleId, roleId)
            .eq(SysRoleRight::getRightType, 1)

        this.remove(wrapper)

    }

}