package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProductionOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProductionOrderMapper
import org.springframework.stereotype.Service

@Service
open class ProductionOrderPlusService : ServiceImpl<ProductionOrderMapper, ProductionOrder>(){

    fun findByNo(prodNo : String) : ProductionOrder{
        val wrapper = MPJLambdaWrapper<ProductionOrder>()
            .select("top 1 *")
            .eq(ProductionOrder::getProdNo, prodNo)

        return this.getOne(wrapper) ?: throw WebException("【${prodNo}】该生产单号不存在")
    }

    fun findById(id : String) : ProductionOrder{
        return this.getById(id) ?: throw WebException("该生产单号不存在")
    }


}
