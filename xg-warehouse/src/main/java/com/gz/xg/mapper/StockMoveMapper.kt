package com.gz.xg.mapper

import com.github.yulichang.base.MPJBaseMapper
import com.gz.xg.domain.entity.StockMove
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StockMoveMapper : TagBaseMapper<StockMove>
