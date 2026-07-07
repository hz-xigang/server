package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.ShipOrder
import com.gz.xg.mapper.ShipOrderMapper
import org.springframework.stereotype.Service

/**
 * 发货单主表底层服务。
 */
@Service
class ShipOrderPlusService : MPJBaseServiceImpl<ShipOrderMapper, ShipOrder>()
