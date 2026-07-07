package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.mapper.StockInventoryMapper
import org.springframework.stereotype.Service

/**
 * 库存底层服务。
 */
@Service
 class StockInventoryPlusService : ServiceImpl<StockInventoryMapper, StockInventory>(){

    /**
     * 根据标签号查询库存记录。
     */
    fun findByTagNo(tagNo: String): StockInventory {
        val wrapper = QueryWrapper<StockInventory>().eq("tagNo", tagNo)
        return getOne(wrapper)
    }
 }
