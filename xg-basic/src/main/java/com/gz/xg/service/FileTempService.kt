package com.gz.xg.service

import com.gz.xg.domain.entity.FileTemp
import com.gz.xg.service.plus.FileTempPlusService
import org.springframework.stereotype.Service

@Service
class FileTempService(
    private val plusService: FileTempPlusService
) {

    fun findById(id: String): FileTemp? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: FileTemp): Boolean {
        return plusService.updateById(entity)
    }
}
