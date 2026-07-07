package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdTagMapper
import com.gz.xg.mapper.VProTagMapper
import org.springframework.stereotype.Service

/**
 * 纸箱标签底层服务，负责标签视图查询和标签号定位。
 */
@Service
 class ProdTagPlusService(
    private  var vProTagMapper: VProTagMapper
) : ServiceImpl<ProdTagMapper, ProdTag>()
{

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listVo(prodNo : String?) : List<ProdTagVo>{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
            .eq(!prodNo.isNullOrBlank(),ProdTagVo::getProdNo,prodNo)

        return vProTagMapper.selectList(wrapper)
    }

    /**
     * 根据标签号查询标签视图，不存在时抛出异常。
     */
    fun findVoByTagNo(tagNo : String) : ProdTagVo{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
        wrapper.eq(ProdTagVo::getTagNo, tagNo)

        return vProTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不存在")
    }

    /**
     * 根据标签号集合批量查询标签视图。
     */
    fun listByTagNos(tagNos : List<String>) : List<ProdTagVo>{
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
            .`in`(ProdTagVo::getTagNo,tagNos)
        return vProTagMapper.selectList(wrapper)
    }
}
