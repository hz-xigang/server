package com.gz.xg.service.plus

import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.mapper.StockMoveMapper
import org.springframework.stereotype.Service

/**
 * 移库单主表关联服务，同时复用标签查询能力。
 */
@Service
class StockMovePlusService : AbstractTagPlusService<StockMoveMapper, StockMove>()
