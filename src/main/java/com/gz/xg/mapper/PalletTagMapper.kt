package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.github.yulichang.base.MPJBaseMapper
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.domain.entity.PalletTag
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface PalletTagMapper : MPJBaseMapper<PalletTag>{

    fun tagExitsByTagNo(tagNo : String) : String?

    fun tagExitsByTagNos(tagNos : List<String>) : List<String>

}
