package com.gz.xg.service

import com.gz.xg.domain.entity.ProductionOrder
import com.gz.xg.service.plus.ProductionOrderPlusService
import org.springframework.stereotype.Service

@Service
open class ProductionOrderService(
    private val plusService: ProductionOrderPlusService
) {

    fun findByProgNo(prodNo: String): ProductionOrder {
        return plusService.findByNo(prodNo)
    }



}
