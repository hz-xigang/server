package com.gz.xg.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.gz.xg.domain.entity.Pallet
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select

@Mapper
interface PalletMapper : BaseMapper<Pallet>{

    fun tagExitsByTagNo(tagNo : String) : String?

    fun tagExitsByTagNos(tagNos : List<String>) : List<String>

}
