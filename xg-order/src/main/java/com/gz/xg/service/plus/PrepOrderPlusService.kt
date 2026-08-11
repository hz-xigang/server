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
            .eq(PrepOrder::getDeleted,0)

        val prepOrder = this.getOne(wrapper)
            ?: throw WebException("【${no}】该备料指令单不存在")

        // PRE-5：只允许未开始(0)或备料中(1)的指令继续备料
        if (prepOrder.status != 0 && prepOrder.status != 1) {
            throw WebException("【${no}】该备料指令已备料完成，不能继续备料")
        }

        return prepOrder
    }


}
