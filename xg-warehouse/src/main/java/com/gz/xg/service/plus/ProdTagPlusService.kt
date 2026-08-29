package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.search.ProdTagSearch
import com.gz.xg.domain.view.VMoveTag
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdTagMapper
import com.gz.xg.mapper.VMoveTagMapper
import com.gz.xg.mapper.VProTagMapper
import com.gz.xg.util.DateUtil
import org.springframework.stereotype.Service

/**
 * 纸箱标签底层服务，负责标签视图查询和标签号定位。
 */
@Service
 class ProdTagPlusService(
      val vProTagMapper: VProTagMapper,
     val vMoveTagMapper: VMoveTagMapper
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
     * 分页查询标签视图，支持按生产单号、操作人姓名、客户编号、存货名称筛选。
     */
    fun page(search: ProdTagSearch, current: Long, size: Long): IPage<VProdTag> {
        val page = Page<VProdTag>(current, size)
        DateUtil.initBaseSearch(search)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it, 1) }

        val wrapper = MPJLambdaWrapper<VProdTag>()
            .between(VProdTag::getCreateTime, search.startDate, search.endDate)
            .like(!search.prodNo.isNullOrBlank(), VProdTag::getProdNo, search.prodNo)
            .like(!search.realName.isNullOrBlank(), VProdTag::getRealName, search.realName)
            .like(!search.customerCode.isNullOrBlank(), VProdTag::getCustomerCode, search.customerCode)
            .like(!search.inventoryName.isNullOrBlank(), VProdTag::getInventoryName, search.inventoryName)
            .orderByDesc(VProdTag::getId)

        return vProTagMapper.selectPage(page, wrapper)
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
    fun listByTagNos(tagNos: List<String>): List<VProdTag> =
        listByTagNos(tagNos, vProTagMapper)

    fun moveTagListByTagNos(tagNos: List<String>): List<VMoveTag> =
        listByTagNos(tagNos, vMoveTagMapper)

    private fun <T : VProdTag> listByTagNos(
        tagNos: List<String>,
        mapper: BaseMapper<T>
    ): List<T> {
        if (tagNos.isEmpty()) return emptyList()

        return mapper.selectList(
            LambdaQueryWrapper<T>()
                .`in`(VProdTag::getTagNo, tagNos)
        )
    }


    fun findById(id : String) : VProdTag{
        val vProdTag = vProTagMapper.selectById(id)
        return vProdTag ?: throw WebException("该纸箱标签不存在")
    }
}