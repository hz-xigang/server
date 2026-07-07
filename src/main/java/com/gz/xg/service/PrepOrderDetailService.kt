package com.gz.xg.service

import com.gz.xg.domain.entity.PrepOrderDetail
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import org.springframework.stereotype.Service

/**
 * 备料单明细服务，提供明细数据的基础增删改查能力。
 */
@Service
open class PrepOrderDetailService(
    private val plusService: PrepOrderDetailPlusService
) {

    /**
     * 根据主键查询备料单明细。
     */
    fun findById(id: String): PrepOrderDetail? {
        return plusService.getById(id)
    }

    /**
     * 根据主键删除备料单明细。
     */
    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    /**
     * 根据主键更新备料单明细。
     */
    fun updateById(entity: PrepOrderDetail): Boolean {
        return plusService.updateById(entity)
    }
}
