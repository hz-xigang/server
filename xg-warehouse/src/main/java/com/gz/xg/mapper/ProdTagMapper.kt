package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.view.VStockTag
import org.apache.ibatis.annotations.Mapper

@Mapper
interface ProdTagMapper : BaseMapper<ProdTag>{

    fun  listByPrep(prepId : String) : List<VStockTag>

}
