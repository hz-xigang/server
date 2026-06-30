package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.mapper.ShipOrderMapper
import org.springframework.stereotype.Service

@Service
class ShipOrderPlusService : MPJBaseServiceImpl<ShipOrderMapper, ShipOrder>()
