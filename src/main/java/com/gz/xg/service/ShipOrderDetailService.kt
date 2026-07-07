package com.gz.xg.service

import com.gz.xg.domain.entity.ShipOrderDetail
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import org.springframework.stereotype.Service

/**
 * 发货单明细服务，当前主要作为明细数据访问的门面。
 */
@Service
class ShipOrderDetailService(
    private val plusService: ShipOrderDetailPlusService
) {

}
