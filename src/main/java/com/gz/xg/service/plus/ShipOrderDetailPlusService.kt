package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.github.yulichang.query.MPJLambdaQueryWrapper
import com.github.yulichang.query.MPJQueryWrapper
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.mapper.ShipOrderDetailMapper
import org.springframework.stereotype.Service

/**
 * 发货单明细底层服务。
 */
@Service
class ShipOrderDetailPlusService : MPJBaseServiceImpl<ShipOrderDetailMapper, ShipOrderDetail>(){

    /**
     * 按发货单主表主键集合查询明细。
     */
    fun listByPIds(pIds : List<String>) : List<ShipOrderDetail>{
        if (pIds.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<ShipOrderDetail>().`in`(ShipOrderDetail::getPId, pIds)
        return list(wrapper)
    }
}
