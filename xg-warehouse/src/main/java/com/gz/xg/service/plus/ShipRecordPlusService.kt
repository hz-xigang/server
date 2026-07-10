package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.mapper.ShipRecordMapper
import org.springframework.stereotype.Service

@Service
class ShipRecordPlusService : ServiceImpl<ShipRecordMapper, ShipRecord>()
