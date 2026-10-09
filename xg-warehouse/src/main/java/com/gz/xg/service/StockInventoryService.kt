package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.view.VLocInventorySummary
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.domain.view.VStockTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.VLocInventorySummaryMapper
import com.gz.xg.mapper.VProTagMapper
import com.gz.xg.mapper.VStockTagMapper
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

/**
 * 库存服务，负责标签入库后的库存生成和库位调整。
 */
@Service
 class StockInventoryService(
    private val plusService: StockInventoryPlusService,
     private val vLocInventorySummaryMapper: VLocInventorySummaryMapper,
     private val vStockTagMapper: VStockTagMapper,
     private val prodTagPlusService: ProdTagPlusService,
     private val locArchivePlusService: LocArchivePlusService
) : BaseService()
{

    /**
     * 按标签批量新增库存记录。
     */
    fun addBatch(prodTags: List<VProdTag>, loc: LocArchive, u8BatchByTagNo: Map<String, String>, warehouseCode: String? = null){
        val list = arrayListOf<StockInventory>()
        prodTags.forEach {
            val stock = StockInventory()
            stock.id = IdUtil.generateId()
            stock.tagNo = it.tagNo
            stock.locCode = loc.locCode
            stock.locId = loc.id
            stock.warehouseCode = warehouseCode
            stock.qty = it.qty
            stock.grossWeight = it.grossWeight
            stock.netWeight = it.netWeight
            stock.u8Batch = u8BatchByTagNo[it.tagNo] ?: ""

            list.add(stock)
        }
        plusService.saveBatch(list)
    }

    /**
     * 按标签批量更新库存所在库位。
     */

    fun editLoc(prodTags: List<VProdTag>, loc: LocArchive, targetWarehouseCode: String? = null){
        val tagNos = prodTags.map { it.tagNo }
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(StockInventory::getLocId,loc.id)
            .set(StockInventory::getLocCode,loc.locCode)
            .set(!targetWarehouseCode.isNullOrBlank(), StockInventory::getWarehouseCode, targetWarehouseCode)
            .eq(StockInventory::getDeleted, 0)
            .`in`(StockInventory::getTagNo,tagNos)
            .update()
    }

    fun editLocByTagNo(tagNos: List<String>, loc: LocArchive, targetWarehouseCode: String? = null){
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(StockInventory::getLocId,loc.id)
            .set(StockInventory::getLocCode,loc.locCode)
            .set(!targetWarehouseCode.isNullOrBlank(), StockInventory::getWarehouseCode, targetWarehouseCode)
            .eq(StockInventory::getDeleted, 0)
            .`in`(StockInventory::getTagNo,tagNos)
            .update()
    }


    fun changeDelByTagNos(tagNos: List<String>){
        super.changeDel(
            plusService.baseMapper,
            StockInventory::getDeleted,
            1){
            eq(StockInventory::getDeleted, 0)
            `in`(StockInventory::getTagNo,tagNos)
        }
    }

    fun inventorySummary() : List<VLocInventorySummary>{
        return  LambdaQueryChainWrapper(vLocInventorySummaryMapper)
            .orderByAsc(VLocInventorySummary::getId)
            .list()
    }

    fun findTagByLocId(locId : String) : List<VStockTag>{
        return LambdaQueryChainWrapper(vStockTagMapper)
            .eq(VStockTag::getLocId,locId)
            .list()
    }

    /**
     * 根据扫描的纸箱标签号，查询相同存货编码的所有在库条码明细。
     */
    fun findTagsByTagNo(tagNo: String): List<VStockTag> {
        val tag = prodTagPlusService.findVoByTagNo(tagNo)
        val inventoryCode = tag.inventoryCode
        if (inventoryCode.isNullOrBlank()) {
            return emptyList()
        }
        return LambdaQueryChainWrapper(vStockTagMapper)
            .eq(VProdTag::getInventoryCode, inventoryCode)
            .list()
    }

    /**
     * 根据扫描的库位编码，查询该库位当前存放的所有在库条码明细。
     */
    fun findTagsByLocCode(locCode: String): List<VStockTag> {
        locArchivePlusService.exitsByCode(locCode)
            ?: throw WebException("【${locCode}】该库位不存在")
        return LambdaQueryChainWrapper(vStockTagMapper)
            .eq(VStockTag::getLocCode, locCode)
            .list()
    }


}