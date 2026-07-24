package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.UserContext
import com.gz.xg.domain.dto.PrepOrderDto
import com.gz.xg.domain.dto.TransferOrderDto
import com.gz.xg.domain.entity.PrepOrder
import com.gz.xg.domain.mapstruct.PrepOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.PrepOrderMapStruct
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import com.gz.xg.service.plus.PrepOrderPlusService
import com.gz.xg.util.IdUtil

import org.springframework.stereotype.Service

/**
 * 备料单服务，负责主表及明细的新增和分页查询。
 */
@Service
class PrepOrderService(
    private val plusService: PrepOrderPlusService,
    private val detailPlusService: PrepOrderDetailPlusService,
    private val mapStruct: PrepOrderMapStruct,
    private val detailMapStruct: PrepOrderDetailMapStruct,
    private val sysSequenceService: SysSequenceService
) {

    /**
     * 新增备料单，并为主表和明细生成主键及单号。
     */
    fun add(prepOrderDto: PrepOrderDto){
        val details = detailMapStruct.toEntityList(prepOrderDto.details)

        val prepOrder = mapStruct.toEntity(prepOrderDto)
        prepOrder.id = IdUtil.generateId()
        prepOrder.prepNo = sysSequenceService.generatePrep()
        val (userId, username,realName) = UserContext.require()
        prepOrder.userId = userId
        prepOrder.username = username
        prepOrder.realName = realName

        details.forEach {
            it.id = IdUtil.generateId()
            it.orderId =  prepOrder.id
            it.orderType = prepOrder.orderType
        }

        plusService.save(prepOrder)
        detailPlusService.saveBatch(details)
    }

    /**
     * 分页查询备料单，并回填每条主表记录对应的明细列表。
     */
    fun page(current : Long, size : Long) : Map<String, Any>{
        val page = Page<PrepOrder>(current, size)
        val wrapper = LambdaQueryWrapper<PrepOrder>()
            .orderByDesc(PrepOrder::getId)

        val pageObj = plusService.page(page, wrapper)
        val dtoList = mapStruct.toDtoList(pageObj.records)

        val ids = dtoList.map { it.id }
        val details = detailPlusService.listByPIds(ids)
        val detailMap = detailMapStruct.toDtoList(details).groupBy { it.orderId }
        dtoList.forEach {
            it.details  = detailMap[it.id].orEmpty()
        }

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }

    fun list() : List<PrepOrderDto>{
        val list = plusService.getList()
        val dtoList = mapStruct.toDtoList(list)
        return dtoList
    }

}
