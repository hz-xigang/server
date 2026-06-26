package com.gz.xg.service

import com.gz.xg.domain.entity.StockIn
import com.gz.xg.service.plus.StockInPlusService
import org.springframework.stereotype.Service

@Service
open class StockInService(
    private val plusService: StockInPlusService
) {

    fun findById(id: String): StockIn? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: StockIn): Boolean {
        return plusService.updateById(entity)
    }
}
