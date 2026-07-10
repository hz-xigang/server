package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.StockInventory
import org.apache.ibatis.annotations.Mapper

@Mapper
interface StockInventoryMapper : BaseMapper<StockInventory>