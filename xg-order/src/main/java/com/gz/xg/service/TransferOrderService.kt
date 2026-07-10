package com.gz.xg.service


import com.gz.xg.domain.dto.TransferOrderDto
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.domain.mapstruct.TransferOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.TransferOrderMapStruct
import com.gz.xg.domain.search.TransferOrderSearch
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import com.gz.xg.service.plus.TransferOrderPlusService
import org.springframework.stereotype.Service

/**
 * 调拨单服务，负责调拨单分页查询和明细组装。
 */
@Service
class TransferOrderService(
    private val plusService: TransferOrderPlusService,
    private val detailPlusService: TransferOrderDetailPlusService,
    private val mapStruct: TransferOrderMapStruct,
    private val detailMapStruct: TransferOrderDetailMapStruct
)  {

    /**
     * 分页查询调拨单，并回填对应的明细记录。
     */
    fun page(current: Long, size: Long,search: TransferOrderSearch) : Map<String, Any>{
        val pageObj = plusService.page(current, size,search)

        val dtoList = mapStruct.toDtoList(pageObj.records)
        val ids = dtoList.map { it.id }
        val details = detailPlusService.listByPIds(ids)
        val detailMap = detailMapStruct.toDtoList(details).groupBy { it.pId }

        dtoList.forEach {
            it.details  = detailMap[it.id].orEmpty()
        }

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }

    fun list() : List<TransferOrderDto>{
        val list = plusService.getList()
        val dtoList = mapStruct.toDtoList(list)
        return dtoList
    }

}
