package com.gz.xg.mapper

import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.StockOutTag
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StockOutTagMapper : TagBaseMapper<StockOutTag>
