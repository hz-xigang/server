package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.domain.search.TransferOrderSearch
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.TransferOrderMapper
import org.springframework.stereotype.Service

/**
 * 调拨单主表底层服务，封装分页查询条件。
 */
@Service
class TransferOrderPlusService : ServiceImpl<TransferOrderMapper, TransferOrder>(){

    /**
     * 按查询条件分页获取调拨单数据。
     */
    fun page(current: Long, size: Long,search: TransferOrderSearch) : Page<TransferOrder>{
        val page = Page<TransferOrder>(current, size)
        val wrapper = LambdaQueryWrapper<TransferOrder>()
            .orderByDesc(TransferOrder::getId)
            .between(TransferOrder::getCreateTime,search.startDate,search.endDate)
            .and(!search.keyword.isNullOrBlank()) {
                it.like(TransferOrder::getOrderNo, search.keyword)
                    .or()
                    .like(TransferOrder::getApplyNo, search.keyword)
            }

        return this.page(page,wrapper)
    }


    fun getList() : List<TransferOrder>{
        val wrapper = LambdaQueryWrapper<TransferOrder>()
            .orderByDesc(TransferOrder::getId)
            .eq(TransferOrder::getStatus,0)

        return this.list(wrapper)
    }

    fun findByNo(no : String?) :TransferOrder {
        val wrapper = LambdaQueryWrapper<TransferOrder>()
            .eq(TransferOrder::getOrderNo, no)

        return this.getOne(wrapper)
            ?: throw WebException("【${no}】该调拨指令单不存在")
    }

}
