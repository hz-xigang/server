package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.conditions.Wrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.github.yulichang.base.MPJBaseMapper
import com.gz.xg.domain.entity.ProdOrder
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface ProdOrderMapper : MPJBaseMapper<ProdOrder>{

    fun pageCustomerOrderNo(
        @Param("ew") wrapper: Wrapper<ProdOrder>,
        @Param("offSet") offSet: Int,
        @Param("next") next: Int
    ) : List<ProdOrder>

    fun pageCustomerOrderNoTotal( @Param("ew") wrapper: Wrapper<ProdOrder>) : Long

}
