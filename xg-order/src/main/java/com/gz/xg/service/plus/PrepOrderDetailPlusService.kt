package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.PrepOrderDetail
import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.mapper.PrepOrderDetailMapper
import org.springframework.stereotype.Service

/**
 * 备料单明细底层服务。
 */
@Service
class PrepOrderDetailPlusService : ServiceImpl<PrepOrderDetailMapper, PrepOrderDetail>(){

    /**
     * 按备料单主表主键集合查询明细。
     */
    fun listByPIds(pIds: List<String>): List<PrepOrderDetail> {
        if (pIds.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<PrepOrderDetail>().`in`(PrepOrderDetail::getOrderId, pIds)
        return list(wrapper)
    }
}
