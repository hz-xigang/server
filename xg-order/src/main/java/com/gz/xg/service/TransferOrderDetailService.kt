package com.gz.xg.service

import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import org.springframework.stereotype.Service

/**
 * 调拨单明细服务，提供明细基础维护能力。
 */
@Service
open class TransferOrderDetailService(
    private val plusService: TransferOrderDetailPlusService
) {

    /**
     * 根据主键查询调拨单明细。
     */
    fun findById(id: String): TransferOrderDetail? {
        return plusService.getById(id)
    }

    /**
     * 根据主键删除调拨单明细。
     */
    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    /**
     * 根据主键更新调拨单明细。
     */
    fun updateById(entity: TransferOrderDetail): Boolean {
        return plusService.updateById(entity)
    }
}
