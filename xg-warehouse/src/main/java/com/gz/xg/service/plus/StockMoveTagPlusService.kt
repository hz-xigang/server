package com.gz.xg.service.plus


import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.mapper.StockMoveTagMapper
import org.springframework.stereotype.Service

/**
 * 移库标签关联底层服务。
 */
@Service
class StockMoveTagPlusService(
   
) : AbstractTagPlusService<StockMoveTagMapper, StockMoveTag>(){

}
