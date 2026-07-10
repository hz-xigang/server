package com.gz.xg.service

import com.gz.xg.domain.entity.StockOut
import com.gz.xg.service.plus.StockOutPlusService
import org.springframework.stereotype.Service

@Service
class StockOutService(
    private val plusService: StockOutPlusService
) {

    fun findById(id: String): StockOut? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: StockOut): Boolean {
        return plusService.updateById(entity)
    }
}
