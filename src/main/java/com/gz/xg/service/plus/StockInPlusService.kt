package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.mapper.StockInMapper
import org.springframework.stereotype.Service

@Service
open class StockInPlusService : ServiceImpl<StockInMapper, StockIn>()
