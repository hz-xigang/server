package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.view.VProdTag
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
    fun listVo(prodNo : String?) : List<VProdTag>{
        val wrapper = MPJLambdaWrapper<VProdTag>()
            .eq(!prodNo.isNullOrBlank(),VProdTag::getProdNo,prodNo)

        return vProTagMapper.selectList(wrapper)
    }

    /**
     * 根据标签号查询标签视图，不存在时抛出异常。
     */
    fun findVoByTagNo(tagNo : String) : VProdTag{
        val wrapper = MPJLambdaWrapper<VProdTag>()
        wrapper.eq(VProdTag::getTagNo, tagNo)

        return vProTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不存在")
    }

    /**
     * 根据标签号集合批量查询标签视图。
     */
    fun listByTagNos(tagNos : List<String>) : List<VProdTag>{
        val wrapper = MPJLambdaWrapper<VProdTag>()
            .`in`(VProdTag::getTagNo,tagNos)
        return vProTagMapper.selectList(wrapper)
    }
}