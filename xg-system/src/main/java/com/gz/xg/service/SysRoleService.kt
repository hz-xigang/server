package com.gz.xg.service

import com.gz.xg.domain.entity.SysRole
import com.gz.xg.service.plus.SysRolePlusService
import org.springframework.stereotype.Service

@Service
class SysRoleService(
    private val plusService: SysRolePlusService
) {

    fun findById(id: String): SysRole? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: SysRole): Boolean {
        return plusService.updateById(entity)
    }
}
