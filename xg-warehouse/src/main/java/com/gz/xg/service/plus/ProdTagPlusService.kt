package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.search.ProdTagSearch
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdTagMapper
import com.gz.xg.mapper.VProTagMapper
import com.gz.xg.util.DateUtil
import org.springframework.stereotype.Service

/**
 * 纸箱标签底层服务，负责标签视图查询和标签号定位。
 */
@Service
 class ProdTagPlusService(
      val vProTagMapper: VProTagMapper
) : ServiceImpl<ProdTagMapper, ProdTag>()
{

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listVo(search: ProdTagSearch) : List<VProdTag>{
        val wrapper = MPJLambdaWrapper<VProdTag>()
        DateUtil.initBaseSearch(search)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it,1) }

        wrapper.between(VProdTag::getCreateTime,search.startDate,search.endDate)
            .like(!search.prodNo.isNullOrBlank(),VProdTag::getProdNo,search.prodNo)
            .like(!search.customerCode.isNullOrBlank(),VProdTag::getCustomerCode,search.customerCode)
            .like(!search.inventoryName.isNullOrBlank(),VProdTag::getInventoryName,search.inventoryName)

        return vProTagMapper.selectList(wrapper)
    }

    /**
     * 根据标签号查询标签视图，不存在时抛出异常。
     */
    fun findVoByTagNo(tagNo : String) : VProdTag{
        val wrapper = MPJLambdaWrapper<VProdTag>()
        wrapper.eq(VProdTag::getTagNo, tagNo).eq(VProdTag::getDeleted,0)

        return vProTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不存在")
    }

    /**
     * 根据标签号集合批量查询标签视图。
     */
    fun listByTagNos(tagNos : List<String>) : List<VProdTag>{
        if (tagNos.isEmpty()) return emptyList()
        val wrapper = MPJLambdaWrapper<VProdTag>()
            .`in`(VProdTag::getTagNo,tagNos)
        return vProTagMapper.selectList(wrapper)
    }

    fun findById(id : String) : VProdTag{
        val vProdTag = vProTagMapper.selectById(id)
        return vProdTag ?: throw WebException("该纸箱标签不存在")
    }
}