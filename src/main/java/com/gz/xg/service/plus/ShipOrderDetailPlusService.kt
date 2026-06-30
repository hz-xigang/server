package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.github.yulichang.query.MPJLambdaQueryWrapper
import com.github.yulichang.query.MPJQueryWrapper
import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.mapper.ShipOrderDetailMapper
import org.springframework.stereotype.Service

@Service
class ShipOrderDetailPlusService : MPJBaseServiceImpl<ShipOrderDetailMapper, ShipOrderDetail>(){

    fun listByPIds(ids : List<String>) : List<ShipOrderDetail>{
        if (ids.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<ShipOrderDetail>().`in`(ShipOrderDetail::getPId, ids)
        return list(wrapper)
    }

}
