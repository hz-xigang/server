package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.entity.StockOutTag
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.StockOutMapStruct
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.service.plus.StockOutPlusService
import com.gz.xg.service.plus.StockOutTagPlusService
import com.gz.xg.util.DateUtil
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@Service
class StockOutService(
    private val plusService: StockOutPlusService,
    private val stockOutTagPlusService: StockOutTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockOutMapStruct: StockOutMapStruct,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
) {

    /**
     * 发货出库
     */
    fun addByShip(records: List<ShipRecord>, outNo: String) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val tagNos = records.map { it.tagNo }
            val locCodes = records.map { it.loc }.distinct()
            val locArchives = locArchivePlusService.listByCode(locCodes)

            if (locArchives.isEmpty()) {
                throw WebException("仓库都不存在")
            }

            val resolved = billTagResolver.resolve(tagNos)

            // SHP-2：校验所有标签是否在库（StockInventory.deleted=0）
            val inventories = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (inventories.size != resolved.tagNos.size) {
                val inStock = inventories.map { it.tagNo }.toSet()
                val missing = resolved.tagNos - inStock
                throw WebException("【${missing.joinToString(",")}】不在库存中，无法出库")
            }

            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockOut = StockOut().apply {
                this.id = id
                receiptNo = outNo
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
                this.userId = userId
                this.username = username
                this.realName = realName
                type = "发货出库"
                loc = locCodes.joinToString(",")
            }

            val tags = records.map {
                val archive = locArchives.first { lIt -> lIt.locCode == it.loc }
                StockOutTag().apply {
                    pId = id
                    tagNo = it.tagNo
                    locCode = it.loc
                    locId = archive.id
                }
            }

            plusService.save(stockOut)
            stockOutTagPlusService.saveBatch(tags)
            stockInventoryService.changeDelByTagNos(tagNos)
        }
    }

    /**
     * 调拨出库
     */
    fun addByTransfer(records: List<TransferRecord>, outNo: String, locArchive: LocArchive) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val tagNos = records.map { it.tagNo }
            val resolved = billTagResolver.resolve(tagNos)

            // SHP-2：校验所有标签是否在库（StockInventory.deleted=0）
            val inventories = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (inventories.size != resolved.tagNos.size) {
                val inStock = inventories.map { it.tagNo }.toSet()
                val missing = resolved.tagNos - inStock
                throw WebException("【${missing.joinToString(",")}】不在库存中，无法出库")
            }
            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockOut = StockOut().apply {
                this.id = id
                receiptNo = outNo
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
                this.userId = userId
                this.username = username
                this.realName = realName
                type = "调拨出库"
                loc = locArchive.locCode
            }

            val tags = records.map {
                StockOutTag().apply {
                    pId = id
                    tagNo = it.tagNo
                    locCode = locArchive.locCode
                    locId = locArchive.id
                }
            }

            plusService.save(stockOut)
            stockOutTagPlusService.saveBatch(tags)
            stockInventoryService.changeDelByTagNos(tagNos)
        }
    }

    fun page(search: StockSearch, current: Long, size: Long) : Map<String, Any>  {
        val page = Page<StockOut>(current, size)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }

        val wrapper = MPJLambdaWrapper<StockOut>()
            .like(!search.no.isNullOrBlank(), StockIn::getReceiptNo, search.no)
            .between(ProdOrder::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockIn::getId)

        val pageObj = plusService.page(page, wrapper)
        val dtoList = stockOutMapStruct.toDtoList(pageObj.records)
        val ids = dtoList.map { it.id }
        if (ids.isNotEmpty()){
            val allStockInTags = stockOutTagPlusService.listByPIds(ids)
            val tagNosByPId = allStockInTags.groupBy({ it.pId }, { it.tagNo })
            val allTagNos = allStockInTags.map { it.tagNo }
            val prodTagMap = prodTagPlusService.listByTagNos(allTagNos).associateBy { it.tagNo }

            dtoList.forEach { dtoIt ->
                val tagNos = tagNosByPId[dtoIt.id] ?: emptyList()
                dtoIt.tags = tagNos.mapNotNull { prodTagMap[it] }
            }
        }
        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }

}
