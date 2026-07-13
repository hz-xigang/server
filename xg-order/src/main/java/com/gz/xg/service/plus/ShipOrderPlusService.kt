package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ShipOrderMapper
import org.springframework.stereotype.Service

/**
 * 发货单主表底层服务。
 */
@Service
class ShipOrderPlusService : MPJBaseServiceImpl<ShipOrderMapper, ShipOrder>(){

    fun getList() : List<ShipOrder>{
        val wrapper = LambdaQueryWrapper<ShipOrder>()
            .orderByDesc(ShipOrder::getId)
            .eq(ShipOrder::getStatus,0)

        return this.list(wrapper)
    }


    fun findByNo(no : String?) :ShipOrder{
        val wrapper = LambdaQueryWrapper<ShipOrder>()
            .eq(ShipOrder::getShipNo,no)

        return this.getOne(wrapper)
            ?: throw WebException("【${no}】该发货指令单不存在")
    }
}
