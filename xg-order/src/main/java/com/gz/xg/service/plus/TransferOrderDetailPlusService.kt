package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.mapper.TransferOrderDetailMapper
import org.springframework.stereotype.Service

/**
 * 调拨单明细底层服务。
 */
@Service
class TransferOrderDetailPlusService : ServiceImpl<TransferOrderDetailMapper, TransferOrderDetail>(){

    /**
     * 按调拨单主表主键集合查询明细。
     */
    fun listByPIds(pIds: List<String>): List<TransferOrderDetail> {
        if (pIds.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<TransferOrderDetail>().`in`(TransferOrderDetail::getPId, pIds)
        return list(wrapper)
    }

    fun listById(pId : String) : List<TransferOrderDetail> {
        val wrapper = LambdaQueryWrapper<TransferOrderDetail>().eq(TransferOrderDetail::getPId, pId)
        return list(wrapper)
    }

}
