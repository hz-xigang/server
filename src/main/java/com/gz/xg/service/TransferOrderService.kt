package com.gz.xg.service


import com.gz.xg.domain.mapstruct.TransferOrderDetailMapStruct
import com.gz.xg.domain.mapstruct.TransferOrderMapStruct
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import com.gz.xg.service.plus.TransferOrderPlusService
import org.springframework.stereotype.Service

@Service
class TransferOrderService(
    private val plusService: TransferOrderPlusService,
    private val detailPlusService: TransferOrderDetailPlusService,
    private val mapStruct: TransferOrderMapStruct,
    private val detailMapStruct: TransferOrderDetailMapStruct
)  {

    fun page(current: Long, size: Long) : Map<String, Any>{
        val pageObj = plusService.page(current, size)

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

}
