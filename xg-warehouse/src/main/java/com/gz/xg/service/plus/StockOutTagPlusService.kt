package com.gz.xg.service.plus

import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.StockOutTag
import com.gz.xg.domain.view.VStockTag

import com.gz.xg.exception.WebException
import com.gz.xg.mapper.StockInTagMapper
import com.gz.xg.mapper.StockOutTagMapper
import com.gz.xg.mapper.VStockTagMapper
import org.springframework.stereotype.Service

/**
 * 入库标签关联底层服务，支持库存标签视图查询。
 */
@Service
class StockOutTagPlusService(
    private  var vStockTagMapper: VStockTagMapper
) : AbstractTagPlusService<StockOutTagMapper, StockOutTag>(){

    /**
     * 根据标签号查询库存标签视图。
     */
    fun findVoByTagNo(tagNo : String) : VStockTag{
        val wrapper = MPJLambdaWrapper<VStockTag>()
        wrapper.eq(VStockTag::getTagNo, tagNo)

        return vStockTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不在库存中")
    }
}
