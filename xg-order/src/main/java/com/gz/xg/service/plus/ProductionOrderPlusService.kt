package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdOrderMapper
import org.springframework.stereotype.Service

/**
 * 生产单底层服务，提供按单号和主键校验查询能力。
 */
@Service
open class ProductionOrderPlusService(

) : ServiceImpl<ProdOrderMapper, ProdOrder>(){

    /**
     * 根据生产单号查询生产单，不存在时抛出异常。
     */
    fun findByNo(prodNo : String) : ProdOrder {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .select("top 1 *")
            .eq(ProdOrder::getProdNo, prodNo)

        return this.getOne(wrapper) ?: throw WebException("【${prodNo}】该生产单号不存在")
    }

    /**
     * 根据主键查询生产单，不存在时抛出异常。
     */
    fun findById(id : String) : ProdOrder {
        return this.getById(id) ?: throw WebException("该生产单号不存在")
    }


    fun listByErpOrderNo(erpOrderNo: String): List<ProdOrder> {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .eq(ProdOrder::getErpOrderNo, erpOrderNo)

        val list = super.list(wrapper)
        if (list.isEmpty()){
            throw WebException("【${erpOrderNo}】该单号不存在")
        }
        return list
    }

    fun pageCustomerOrderNo(orderNo : String? ,page: Int,size: Int) : Page<ProdOrder> {
        val wrapper = LambdaQueryWrapper<ProdOrder>()
            .eq(ProdOrder::getType,0)
            .like(!orderNo.isNullOrBlank(), ProdOrder::getErpOrderNo, orderNo)
        val total = super.baseMapper.pageCustomerOrderNoTotal(wrapper)

        wrapper.orderByDesc(ProdOrder::getCreateTime)
        val prodOrders = super.baseMapper.pageCustomerOrderNo(wrapper, (page - 1) * size, size)
        val pageObj = Page<ProdOrder>()
        pageObj.total = total
        pageObj.records = prodOrders

        return pageObj

    }

    fun customerList() : List<ProdOrder> {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .select("DISTINCT customerCode,customerName")
            .isNotNull(ProdOrder::getCustomerName)
            .ne(ProdOrder::getCustomerCode, "")
        return super.list(wrapper)
    }

    /**
     * 根据存货编号查询最新一条有效的生产订单 (type == 2 且 erpOrderId 不为空)
     */
    fun findLatestMomOrderByInvCode(inventoryCode: String): ProdOrder? {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .eq(ProdOrder::getInventoryCode, inventoryCode)
            .eq(ProdOrder::getType, 2)
            .isNotNull(ProdOrder::getErpOrderId)
            .ne(ProdOrder::getErpOrderId, "")
            .orderByDesc(ProdOrder::getCreateTime)

        return this.list(wrapper).firstOrNull()
    }

    /**
     * 根据存货编号和客户编号查询最新一条有效的生产订单 (type == 2 且 erpOrderId 不为空)
     */
    fun findLatestMomOrderByInvCodeAndCustomerCode(inventoryCode: String, customerCode: String?): ProdOrder? {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .eq(ProdOrder::getInventoryCode, inventoryCode)
            .eq(ProdOrder::getType, 2)
            .eq(!customerCode.isNullOrBlank(), ProdOrder::getCustomerCode, customerCode)
            .isNotNull(ProdOrder::getErpOrderId)
            .ne(ProdOrder::getErpOrderId, "")
            .orderByDesc(ProdOrder::getCreateTime)

        val found = this.list(wrapper).firstOrNull()
        if (found != null) {
            return found
        }
        // 若指定客户编号未找到，回退仅按存货编号查找
        return if (!customerCode.isNullOrBlank()) findLatestMomOrderByInvCode(inventoryCode) else null
    }

}