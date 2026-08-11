package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.StockInMapStruct
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.util.DateUtil
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate


/**
 * 入库服务，负责入库单生成、标签关联保存以及库存落库。
 */
@Service
class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val sysSequenceService: SysSequenceService,
    private val stockInMapStruct: StockInMapStruct,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
) {

    /**
     * 新增入库单，并同步写入库存表。
     */
    fun add(req: AddStockIn) {
        addByType(req, null)
    }

    /**
     * 新增退货入库单，并同步写入库存表。
     */
    fun addReturn(req: AddStockIn) {
        addByType(req, "退货入库")
    }

    /**
     * 调拨入库
     */
    fun addByTransfer(records: List<TransferRecord>, outNo: String, locArchive: LocArchive) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val tagNos = records.map { it.tagNo }
            val resolved = billTagResolver.resolve(tagNos)

            // TRN-4 / STK-2：调拨入库前校验是否已在库
            val existing = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (existing.isNotEmpty()) {
                throw WebException("【${existing.joinToString(",") { it.tagNo }}】已在库存中，不能重复入库")
            }

            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockIn = StockIn().apply {
                this.id = id
                receiptNo = outNo
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
                this.userId = userId
                this.username = username
                this.realName = realName
                type = "调拨入库"
                loc = locArchive.locCode
            }

            val tags = records.map {
                StockInTag().apply {
                    pId = id
                    tagNo = it.tagNo
                    locCode = locArchive.locCode
                    locId = locArchive.id
                }
            }

            plusService.save(stockIn)
            stockInTagPlusService.saveBatch(tags)
            stockInventoryService.addBatch(resolved.prodTags, locArchive)
        }
    }

    /**
     * 分页查询入库单，并回填关联标签详情。
     */
    fun page(search: StockSearch, current: Long, size: Long): Map<String, Any> {
        val page = Page<StockIn>(current, size)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }
        val wrapper = MPJLambdaWrapper<StockIn>()
            .like(!search.no.isNullOrBlank(), StockIn::getReceiptNo, search.no)
            .between(ProdOrder::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockIn::getId)

        val pageObj = plusService.page(page, wrapper)

        val dtoList = stockInMapStruct.toDtoList(pageObj.records)

        val ids = dtoList.map { it.id }
        if (ids.isNotEmpty()){
            val allStockInTags = stockInTagPlusService.listByPIds(ids)
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

    private fun addByType(req: AddStockIn, type: String?) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val locArchive = locArchivePlusService.getById(req.locId)
                ?: throw WebException("该库位不存在")

            val resolved = billTagResolver.resolve(req.tagNos)

            // 检查是否已入库
            val occupied = stockInTagPlusService.listByTagNos(resolved.tagNos)
            if (occupied.isNotEmpty()) {
                throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已入库")
            }

            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockIn = StockIn().apply {
                this.id = id
                receiptNo = sysSequenceService.generateStockIn()
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
                loc = locArchive.locCode
                this.type = type
                this.userId = userId
                this.username = username
                this.realName = realName
            }

            val tags = resolved.tagNos.map { tagNo ->
                StockInTag().apply {
                    pId = id
                    this.tagNo = tagNo
                    locId = locArchive.id
                    locCode = locArchive.locCode
                }
            }

            plusService.save(stockIn)
            stockInTagPlusService.saveBatch(tags)
            stockInventoryService.addBatch(resolved.prodTags, locArchive)
        }
    }

}
