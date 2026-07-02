package com.gz.xg.service

import com.gz.xg.domain.entity.PrepOrderDetail
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import org.springframework.stereotype.Service

@Service
open class PrepOrderDetailService(
    private val plusService: PrepOrderDetailPlusService
) {

    fun findById(id: String): PrepOrderDetail? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: PrepOrderDetail): Boolean {
        return plusService.updateById(entity)
    }
}
