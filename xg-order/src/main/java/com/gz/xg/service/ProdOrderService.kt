package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.ProdOrderDto
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.search.ProdOrderSearch
import com.gz.xg.service.plus.ProductionOrderPlusService
import org.slf4j.LoggerFactory
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
            .eq(!search.category.isNullOrBlank(), ProdOrder::getProductCategory, search.category)
            .orderByDesc(ProdOrder::getCreateTime)


        val orderPage = plusService.page(page, wrapper)
        return getPage(orderPage)
    }

    /**
     * 编辑生产单（仅更新页面可编辑字段，不影响单号与创建时间）。
     */
    fun edit(dto: ProdOrderDto) {
        plusService.findById(dto.id)

        LambdaUpdateChainWrapper(plusService.baseMapper)
            .eq(ProdOrder::getId, dto.id)
            .set(dto.erpOrderNo != null, ProdOrder::getErpOrderNo, dto.erpOrderNo)
            .set(dto.inventoryCode != null, ProdOrder::getInventoryCode, dto.inventoryCode)
            .set(dto.inventoryName != null, ProdOrder::getInventoryName, dto.inventoryName)
            .set(dto.customerCode != null, ProdOrder::getCustomerCode, dto.customerCode)
            .set(dto.productCategory != null, ProdOrder::getProductCategory, dto.productCategory)
            .set(dto.spec != null, ProdOrder::getSpec, dto.spec)
            .set(dto.status != null, ProdOrder::getDeleted, !dto.status)
            .update()
        log.info("更新生产单信息成功: id={}, erpOrderNo={}, inventoryCode={}", dto.id, dto.erpOrderNo, dto.inventoryCode)
    }

    /**
     * 软删除生产单（deleted=1）。
     */
    fun softDel(id: String) {
        plusService.findById(id)

        LambdaUpdateChainWrapper(plusService.baseMapper)
            .eq(ProdOrder::getId, id)
            .set(ProdOrder::getDeleted, true)
            .update()
        log.info("软删除生产单成功: id={}", id)
    }
}
