package com.gz.xg.service

import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.service.plus.ShipRecordPlusService
import org.springframework.stereotype.Service

@Service
class ShipRecordService(
    private val plusService: ShipRecordPlusService
) {


}
