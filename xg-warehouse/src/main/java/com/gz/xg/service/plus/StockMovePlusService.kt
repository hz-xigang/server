package com.gz.xg.service.plus

import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.mapper.StockMoveMapper
import org.springframework.stereotype.Service

/**
 * 移库单主表服务
 */
@Service
class StockMovePlusService : MPJBaseServiceImpl<StockMoveMapper, StockMove>()
