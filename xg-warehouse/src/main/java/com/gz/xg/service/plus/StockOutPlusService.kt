package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.mapper.StockOutMapper
import org.springframework.stereotype.Service

@Service
class StockOutPlusService : ServiceImpl<StockOutMapper, StockOut>()
