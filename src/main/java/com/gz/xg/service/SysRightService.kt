package com.gz.xg.service

import com.gz.xg.domain.entity.SysRight
import com.gz.xg.service.plus.SysRightPlusService
import org.springframework.stereotype.Service

@Service
class SysRightService(
    private val plusService: SysRightPlusService
) {

    fun findById(id: String): SysRight? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: SysRight): Boolean {
        return plusService.updateById(entity)
    }
}
