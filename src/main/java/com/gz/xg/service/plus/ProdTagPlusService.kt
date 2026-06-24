package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdTagMapper
import com.gz.xg.mapper.VProTagMapper
import org.springframework.stereotype.Service

@Service
 class ProdTagPlusService(
    private  var vProTagMapper: VProTagMapper
) : ServiceImpl<ProdTagMapper, ProdTag>()
{

    fun listVo(prodNo : String?) : List<ProdTagVo>{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
            .eq(!prodNo.isNullOrBlank(),ProdTagVo::getProdNo,prodNo)

        return vProTagMapper.selectList(wrapper)
    }

    fun findVoByTagNo(tagNo : String) : ProdTagVo{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
        wrapper.eq(ProdTagVo::getTagNo, tagNo)

        return vProTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不存在")
    }

    fun listByTagNos(tagNos : List<String>) : List<ProdTagVo>{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
            .`in`(ProdTagVo::getTagNo,tagNos)
        return vProTagMapper.selectList(wrapper)
    }

}
