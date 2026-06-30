package com.gz.xg.service

import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import org.springframework.stereotype.Service

@Service
class ShipOrderDetailService(
    private val plusService: ShipOrderDetailPlusService
) {

}
