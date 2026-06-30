package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.mapper.TransferOrderDetailMapper
import org.springframework.stereotype.Service

@Service
class TransferOrderDetailPlusService : ServiceImpl<TransferOrderDetailMapper, TransferOrderDetail>(){

    fun listByPIds(pIds: List<String>): List<TransferOrderDetail> {
        if (pIds.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<TransferOrderDetail>().`in`(TransferOrderDetail::getPId, pIds)
        return list(wrapper)
    }

}
