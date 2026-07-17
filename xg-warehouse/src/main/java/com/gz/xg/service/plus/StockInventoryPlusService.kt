package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.exception.WebException
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
    fun findByTagNo(tagNo: String): StockInventory? {
        val wrapper = LambdaQueryWrapper<StockInventory>()
            .eq(StockInventory::getTagNo, tagNo)
            .eq(StockInventory::getDeleted,0)

        return getOne(wrapper)
    }

    /**
     * 根据标签号批量查询库存记录。
     */
    fun listByTagNos(tagNos: List<String>): List<StockInventory> {
        if (tagNos.isEmpty()) return emptyList()
        val wrapper = LambdaQueryWrapper<StockInventory>()
            .`in`(StockInventory::getTagNo, tagNos)
            .eq(StockInventory::getDeleted,0)
        return list(wrapper)
    }

    fun assertNotExists(tagNo: String) {
        if (findByTagNo(tagNo) != null) throw WebException("【$tagNo】该条码未出库")
    }

 }
