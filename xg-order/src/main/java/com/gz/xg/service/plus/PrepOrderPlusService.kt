package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PrepOrderMapper
import org.springframework.stereotype.Service

/**
 * 备料单主表底层服务。
 */
@Service
class PrepOrderPlusService : ServiceImpl<PrepOrderMapper, PrepOrder>(){

    fun getList() : List<PrepOrder>{
        val wrapper = LambdaQueryWrapper<PrepOrder>()
            .orderByDesc(PrepOrder::getId)
            .eq(PrepOrder::getStatus,0)

        return this.list(wrapper)
    }

    fun findByNo(no : String?) : PrepOrder{
        val wrapper = LambdaQueryWrapper<PrepOrder>()
            .eq(PrepOrder::getPrepNo,no)

        return this.getOne(wrapper)
            ?: throw WebException("【${no}】该发货指令单不存在")
    }


}
