package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.entity.PrepOrderDetail
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.TransferOrderDetail
import com.gz.xg.mapper.PrepOrderDetailMapper
import org.springframework.stereotype.Service

/**
 * 备料单明细底层服务。
 */
@Service
class PrepOrderDetailPlusService : MPJBaseServiceImpl<PrepOrderDetailMapper, PrepOrderDetail>(){

    /**
     * 按备料单主表主键集合查询明细。
     */
    fun listByPIds(pIds: List<String>): List<PrepOrderDetail> {
        if (pIds.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<PrepOrderDetail>().`in`(PrepOrderDetail::getOrderId, pIds)
        return list(wrapper)
    }

    fun listByPId(pId: String): List<PrepOrderDetail> {
        val wrapper = LambdaQueryWrapper<PrepOrderDetail>().eq(PrepOrderDetail::getOrderId, pId)
        return list(wrapper)
    }


}
