package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.github.yulichang.query.MPJQueryWrapper
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PalletTagMapper
import com.gz.xg.mapper.ProdTagMapper
import org.springframework.stereotype.Service

@Service
class PalletTagPlusService : MPJBaseServiceImpl<PalletTagMapper, PalletTag>( ) {

    fun findByTagNo(tagNo: String): PalletTag? {
        val wrapper = MPJLambdaWrapper<PalletTag>()
            .eq(PalletTag::getTagNo,tagNo)

        return this.getOne(wrapper)
    }

    fun listByTagNos(tagNos : List<String>) : List<PalletTag> {
        val wrapper = MPJLambdaWrapper<PalletTag>()
            .`in`(PalletTag::getTagNo,tagNos)

        return list(wrapper)
    }

}