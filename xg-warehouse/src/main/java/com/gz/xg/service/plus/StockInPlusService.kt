package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.mapper.StockInMapper
import org.springframework.stereotype.Service

/**
 * 入库单主表底层服务。
 */
@Service
 class StockInPlusService : ServiceImpl<StockInMapper, StockIn>()