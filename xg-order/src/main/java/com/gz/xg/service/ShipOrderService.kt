package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.PrepOrderDto
import com.gz.xg.domain.dto.ShipOrderDto
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.domain.mapstruct.ShipOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.ShipOrderMapStruct
import com.gz.xg.domain.search.ShipOrderSearch
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import com.gz.xg.service.plus.ShipOrderPlusService
import com.gz.xg.util.DateUtil

import org.springframework.stereotype.Service

/**
 * 发货单服务，负责发货单分页查询及明细回填。
 */
@Service
class ShipOrderService(
    private val plusService: ShipOrderPlusService,
    private val detailService: ShipOrderDetailPlusService,
    private val mapStruct: ShipOrderMapStruct,
    private val detailMapStruct: ShipOrderDetailMapStruct
) : BaseService() {

    /**
     * 分页查询发货单，并为每条记录组装明细列表。
     */
    fun page(current: Long, size: Long,search: ShipOrderSearch): Map<String, Any> {
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it,1) }

        val page = Page<ShipOrder>(current, size)
        val wrapper = LambdaQueryWrapper<ShipOrder>()
            .orderByDesc(ShipOrder::getId)
            .like(!search.shipNo.isNullOrBlank(), ShipOrder::getShipNo,search.shipNo)
            .like(!search.customerNo.isNullOrBlank(), ShipOrder::getCustomerCode,search.customerNo)
            .eq(search.isTax != null, ShipOrder::getIsTax,search.isTax)
            .between(ShipOrder::getCreateTime,search.startDate,search.endDate)

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

    /**
     * 列表
     */
    fun list() : List<ShipOrderDto>{
        val list = plusService.getList()
        val dtoList = mapStruct.toDtoList(list)
        return dtoList
    }
}
