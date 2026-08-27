package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
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
    private val prepRecordService: PrepRecordService
) : BaseService() {

    /**
     * 新增入库单，并同步写入库存表。
     */
    fun add(req: AddStockIn) {
        addByType(req, "直接入库")
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

            saveStockIn(resolved, "调拨入库", outNo, locArchive)
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
            .like(!search.locCode.isNullOrBlank(), StockIn::getLoc, search.locCode)
            .between(StockIn::getCreateTime, search.startDate, search.endDate)
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

    private fun addByType(req: AddStockIn, type: String) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val locArchive = locArchivePlusService.getById(req.locId)
                ?: throw WebException("该库位不存在")

            val resolved = billTagResolver.resolve(req.tagNos)

            // STK-1：防重复入库改为查库存表（StockInventory.deleted=0），而非入库流水表
            // 流水表同一 tagNo 可有多条（首次+退货+调拨），历史上入过库即被误拦；
            // 文档规则：当前在库（deleted=0）才拦截
            val occupied = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (occupied.isNotEmpty()) {
                throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已在库存中，不能重复入库")
            }

            saveStockIn(resolved, type, sysSequenceService.generateStockIn(), locArchive)
        }
    }

    /**
     * STK-6：入库单保存公共逻辑（普通/退货/调拨入库共用）。
     */
    private fun saveStockIn(
        resolved: ResolvedTags,
        type: String,
        receiptNo: String,
        locArchive: LocArchive
    ) {
        val id = IdUtil.generateId()
        val (userId, username, realName) = UserContext.require()

        val stockIn = StockIn().apply {
            this.id = id
            this.receiptNo = receiptNo
            qty = resolved.total.qty
            grossWeight = resolved.total.grossWeight
            netWeight = resolved.total.netWeight
            this.type = type
            loc = locArchive.locCode
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
        log.info("入库单保存完成: receiptNo={}, type={}, loc={}, 标签数量={}, 总重量={}", 
            receiptNo, type, locArchive.locCode, tags.size, resolved.total.grossWeight)
    }

}
