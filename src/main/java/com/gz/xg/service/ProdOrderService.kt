package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.search.ProdOrderSearch
import com.gz.xg.service.plus.ProductionOrderPlusService
import org.springframework.stereotype.Service

/**
 * 生产单服务，负责生产单查询及分页检索。
 */
@Service
 class ProdOrderService(
    private val plusService: ProductionOrderPlusService
) : BaseService(){

    /**
     * 根据生产单号查询生产单。
     */
    fun findByProgNo(prodNo: String): ProdOrder {
        return plusService.findByNo(prodNo)
    }

    /**
     * 分页查询生产单，支持日期、关键字和状态过滤。
     */
    fun page(current: Long, size: Long, search: ProdOrderSearch) : Map<String, Any> {
        val page = Page<ProdOrder>(current, size)

        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .between(ProdOrder::getCreateTime, search.startDate, search.endDate)
            .and { w ->
                run {
                    w.like(ProdOrder::getProdNo, search.keyword)
                        .or()
                        .like(ProdOrder::getErpOrderNo, search.keyword)
                }
            }
            .eq(search.status != null,ProdOrder::getDeleted,search.status)
            .orderByDesc(ProdOrder::getCreateTime)


        val orderPage = plusService.page(page, wrapper)
        return getPage(orderPage)
    }
}
