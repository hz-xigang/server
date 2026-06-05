package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.ProductionOrder
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ProductionOrderMapper : BaseMapper<ProductionOrder>
