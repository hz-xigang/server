package com.gz.xg.service.plus

import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.mapper.StockMoveMapper
import org.springframework.stereotype.Service

@Service
class StockMovePlusService : AbstractTagPlusService<StockMoveMapper, StockMove>()
