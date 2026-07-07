package com.gz.xg.service

import com.gz.xg.domain.entity.SysUser
import com.gz.xg.service.plus.SysUserPlusService
import org.springframework.stereotype.Service

@Service
class SysUserService(
    private val plusService: SysUserPlusService
) {

    fun findById(id: String): SysUser? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: SysUser): Boolean {
        return plusService.updateById(entity)
    }
}
