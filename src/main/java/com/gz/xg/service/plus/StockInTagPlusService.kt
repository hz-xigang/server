package com.gz.xg.service.plus

import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.mapper.StockInTagMapper
import org.springframework.stereotype.Service

@Service
class StockInTagPlusService : AbstractTagPlusService<StockInTagMapper, StockInTag>()