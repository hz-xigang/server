package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.entity.StockOutTag
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.StockInMapStruct
import com.gz.xg.domain.mapstruct.StockOutMapStruct
import com.gz.xg.domain.search.StockInSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.service.plus.StockOutPlusService
import com.gz.xg.service.plus.StockOutTagPlusService
import com.gz.xg.util.DateUtil
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal
import kotlin.collections.mapNotNull

@Service
class StockOutService(
    private val plusService: StockOutPlusService,
    private val stockOutTagPlusService: StockOutTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val sysSequenceService: SysSequenceService,
    private val stockOutMapStruct: StockOutMapStruct,
    private val stockInventoryService: StockInventoryService
) {

    /**
     * 发货出库
     */
    fun addByShip(records: List<ShipRecord>, outNo: String) {
        val tagNos = records.map { it.tagNo }
        val locCodes = records.map { it.loc }.distinct()
        val locArchives = locArchivePlusService.listByCode(locCodes)

        if (locArchives.isEmpty()) {
            throw WebException("仓库都不存在")
        }

        val total = calcTotal(tagNos)
        val id = IdUtil.generateId()

        val stockOut = createStockOut(
            id,
            outNo,
            total,
            "发货出库",
            records.map { it.loc }.distinct().joinToString(",")
        )

        val tags = records.map {

            val archive = locArchives.first { lIt ->
                lIt.locCode == it.loc
            }

            StockOutTag().apply {
                pId = id
                tagNo = it.tagNo
                locCode = it.loc
                locId = archive.id
            }
        }

        saveOut(stockOut, tags, tagNos)
    }

    fun addByTransfer(records: List<TransferRecord>, outNo: String, locArchive: LocArchive) {

        val tagNos = records.map { it.tagNo }
        val total = calcTotal(tagNos)
        val id = IdUtil.generateId()

        val stockOut = createStockOut(
            id,
            outNo,
            total,
            "调拨出库",
            locArchive.locCode
        )

        val tags = records.map {
            StockOutTag().apply {
                pId = id
                tagNo = it.tagNo
                locCode = locArchive.locCode
                locId = locArchive.id
            }
        }

        saveOut(stockOut, tags, tagNos)
    }

    private fun calcTotal(tagNos: List<String>): ProdTagTotal {
        val prodTags = prodTagPlusService.listByTagNos(tagNos)

        return prodTags.fold(
            ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)
        ) { acc, item ->
            ProdTagTotal(
                acc.qty + item.qty,
                acc.grossWeight + item.grossWeight,
                acc.netWeight + item.netWeight
            )
        }
    }

    private fun createStockOut(
        id: String,
        outNo: String,
        total: ProdTagTotal,
        type: String,
        loc: String
    ): StockOut {

        val (userId, username,realName) = UserContext.require()

        return StockOut().apply {
            this.id = id
            receiptNo = outNo
            qty = total.qty
            grossWeight = total.grossWeight
            netWeight = total.netWeight
            this.userId = userId
            this.username = username
            this.type = type
            this.loc = loc
        }
    }

    private fun saveOut(
        stockOut: StockOut,
        tags: List<StockOutTag>,
        tagNos: List<String>
    ) {
        plusService.save(stockOut)
        stockOutTagPlusService.saveBatch(tags)
        stockInventoryService.changeDelByTagNos(tagNos)
    }

    fun page(search: StockInSearch,current: Long, size: Long) : Map<String, Any>  {
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
