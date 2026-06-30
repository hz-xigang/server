package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.mapstruct.ShipOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.ShipOrderMapStruct
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import com.gz.xg.service.plus.ShipOrderPlusService

import org.springframework.stereotype.Service

@Service
class ShipOrderService(
    private val plusService: ShipOrderPlusService,
    private val detailService: ShipOrderDetailPlusService,
    private val mapStruct: ShipOrderMapStruct,
    private val detailMapStruct: ShipOrderDetailMapStruct
) : BaseService() {

    fun page(current: Long, size: Long): Map<String, Any> {
        val page = Page<ShipOrder>(current, size)

        val wrapper = MPJLambdaWrapper<ShipOrder>()
            .orderByDesc(ShipOrder::getId)

        val pageObj = plusService.page(page, wrapper)

        val dtoList = mapStruct.toDtoList(pageObj.records)
        val ids = dtoList.map { it.id }
        val details = detailService.listByPIds(ids)
        val detailMap = detailMapStruct.toDtoList(details).groupBy { it.pId }

        dtoList.forEach {
            it.details  = detailMap[it.id].orEmpty()
        }

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }


}
