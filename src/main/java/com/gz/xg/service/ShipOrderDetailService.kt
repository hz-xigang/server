package com.gz.xg.service

import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import org.springframework.stereotype.Service

@Service
open class ShipOrderDetailService(
    private val plusService: ShipOrderDetailPlusService
) {

    fun findById(id: String): ShipOrderDetail? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: ShipOrderDetail): Boolean {
        return plusService.updateById(entity)
    }
}
