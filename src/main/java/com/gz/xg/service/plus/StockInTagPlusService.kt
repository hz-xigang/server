package com.gz.xg.service.plus

import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.domain.vo.StockTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.StockInTagMapper
import com.gz.xg.mapper.VStockTagMapper
import org.springframework.stereotype.Service

@Service
class StockInTagPlusService(
    private  var vStockTagMapper: VStockTagMapper
) : AbstractTagPlusService<StockInTagMapper, StockInTag>(){

    fun findVoByTagNo(tagNo : String) : StockTagVo{
        val wrapper = MPJLambdaWrapper<StockTagVo>()
        wrapper.eq(StockTagVo::getTagNo, tagNo)

        return vStockTagMapper.selectOne(wrapper)
            ?: throw WebException("【${tagNo}】该纸箱标签不存在")
    }

}