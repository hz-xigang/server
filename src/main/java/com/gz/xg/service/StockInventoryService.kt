package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

@Service
 class StockInventoryService(
    private val plusService: StockInventoryPlusService
)
{

    fun addBatch(prodTags: List<ProdTagVo>,loc : LocArchive){
        val list = arrayListOf<StockInventory>()
        prodTags.forEach {
            val stock = StockInventory()
            stock.id = IdUtil.generateId()
            stock.tagNo = it.tagNo
            stock.locCode = loc.locCode
            stock.locId = loc.id
            stock.qty = it.qty
            stock.grossWeight = it.grossWeight
            stock.netWeight = it.netWeight

            list.add(stock)
        }
        plusService.saveBatch(list)
    }

    fun editLoc(prodTags: List<ProdTagVo>,loc : LocArchive){
        val tagNos = prodTags.map { it.tagNo }
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(StockInventory::getLocId,loc.id)
            .set(StockInventory::getLocCode,loc.locCode)
            .`in`(StockInventory::getTagNo,tagNos)
            .update()
    }

}
