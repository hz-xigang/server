package com.gz.xg.service.plus

import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.domain.vo.StockTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.StockInTagMapper
import com.gz.xg.mapper.StockMoveTagMapper
import com.gz.xg.mapper.VStockTagMapper
import org.springframework.stereotype.Service

@Service
class StockMoveTagPlusService(
   
) : AbstractTagPlusService<StockMoveTagMapper, StockMoveTag>(){

}