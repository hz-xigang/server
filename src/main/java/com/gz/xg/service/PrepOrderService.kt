package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.domain.dto.PrepOrderDto
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.mapstruct.PrepOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.PrepOrderMapStruct
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import com.gz.xg.service.plus.PrepOrderPlusService
import com.gz.xg.util.IdUtil

import org.springframework.stereotype.Service

@Service
class PrepOrderService(
    private val plusService: PrepOrderPlusService,
    private val detailPlusService: PrepOrderDetailPlusService,
    private val mapStruct: PrepOrderMapStruct,
    private val detailMapStruct: PrepOrderDetailMapStruct
) {

    fun add(prepOrderDto: PrepOrderDto){
        val details = detailMapStruct.toEntityList(prepOrderDto.details)

        val prepOrder = mapStruct.toEntity(prepOrderDto)
        prepOrder.id = IdUtil.generateId()

        details.forEach {
            it.id = IdUtil.generateId()
            it.orderId =  prepOrder.id
            it.orderType = prepOrder.orderType
        }

        plusService.save(prepOrder)
        detailPlusService.saveBatch(details)
    }

    fun page(current : Long, size : Long) : Map<String, Any>{
        val page = Page<PrepOrder>(current, size)
        val wrapper = LambdaQueryWrapper<PrepOrder>()
            .orderByDesc(PrepOrder::getId)

        val pageObj = plusService.page(page, wrapper)
        val dtoList = mapStruct.toDtoList(pageObj.records)

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )

    }


}
