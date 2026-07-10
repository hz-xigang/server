package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.StockOut
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StockOutMapper : BaseMapper<StockOut>
