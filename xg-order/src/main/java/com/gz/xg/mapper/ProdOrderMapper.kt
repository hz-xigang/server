package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.github.yulichang.base.MPJBaseMapper
import com.gz.xg.domain.entity.ProdOrder
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ProdOrderMapper : MPJBaseMapper<ProdOrder>
