package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.mapper.TransferOrderMapper
import org.springframework.stereotype.Service

@Service
class TransferOrderPlusService : ServiceImpl<TransferOrderMapper, TransferOrder>(){

    fun page(current: Long, size: Long) : Page<TransferOrder>{
        val page = Page<TransferOrder>(current, size)
        val wrapper = LambdaQueryWrapper<TransferOrder>().orderByDesc(TransferOrder::getId)

        return this.page(page,wrapper)
    }

}
