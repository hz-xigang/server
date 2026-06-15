package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.ProdOrderMapper
import org.springframework.stereotype.Service

@Service
open class ProductionOrderPlusService : ServiceImpl<ProdOrderMapper, ProdOrder>(){

    fun findByNo(prodNo : String) : ProdOrder {
        val wrapper = MPJLambdaWrapper<ProdOrder>()
            .select("top 1 *")
            .eq(ProdOrder::getProdNo, prodNo)

        return this.getOne(wrapper) ?: throw WebException("【${prodNo}】该生产单号不存在")
    }

    fun findById(id : String) : ProdOrder {
        return this.getById(id) ?: throw WebException("该生产单号不存在")
    }


}
