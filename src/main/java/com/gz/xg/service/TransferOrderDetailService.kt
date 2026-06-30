package com.gz.xg.service

import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import org.springframework.stereotype.Service

@Service
open class TransferOrderDetailService(
    private val plusService: TransferOrderDetailPlusService
) {

    fun findById(id: String): TransferOrderDetail? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: TransferOrderDetail): Boolean {
        return plusService.updateById(entity)
    }
}
