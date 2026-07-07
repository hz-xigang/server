package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.query.MPJLambdaQueryWrapper
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.dto.StockInDto
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.domain.mapstruct.StockInMapStruct
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockInSearch
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.util.DateUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager

/**
 * 入库服务，负责入库单生成、标签关联保存以及库存落库。
 */
@Service
open class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    prodTagPlusService: ProdTagPlusService,
    pmt: PlatformTransactionManager,
    private val sysSequenceService: SysSequenceService,
    private val stockInMapStruct: StockInMapStruct,
    private val stockInventoryService: StockInventoryService
) : AbstractBillService(prodTagPlusService, pmt) {

    override val tagOccupiedMessage = "已入库"

    override fun generateNo() = sysSequenceService.generateStockIn()

    override fun tagService(): AbstractTagPlusService<*, *> = stockInTagPlusService

    /**
     * 构建入库单主表。
     */
    override fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): StockIn {
        val stockIn = StockIn()
        stockIn.id = id
        stockIn.receiptNo = no
        stockIn.qty = total.qty
        stockIn.grossWeight = total.grossWeight
        stockIn.netWeight = total.netWeight
        stockIn.printUser = "001"
        stockIn.locId = context["locId"] as String
        stockIn.locCode = context["locCode"] as String
        return stockIn
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveBill(entity: Any) {
        plusService.save(entity as StockIn)
    }

    /**
     * 构建入库单和纸箱标签的关联记录。
     */
    override fun buildTagEntry(pId: String, tagNo: String): TagEntity {
        val tag = StockInTag()
        tag.pId = pId
        tag.tagNo = tagNo
        return tag
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveTagBatch(tags: List<TagEntity>) {
        stockInTagPlusService.saveBatch(tags as List<StockInTag>)
    }

    /**
     * 新增入库单，并同步写入库存表。
     */
    fun add(req: AddStockIn) {
        val locId = req.locId
        val tagNos = req.tagNos

        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        val locArchive = locArchivePlusService.getById(locId) ?: throw WebException("该库位不存在")
        doAdd(tagNos, mutableMapOf("locId" to locArchive.id, "locCode" to locArchive.locCode)) { prodTags, _ ->
            run {
                stockInventoryService.addBatch(prodTags, locArchive)
            }
        }
    }

    /**
     * 分页查询入库单，并回填关联标签详情。
     */
    fun page(search: StockInSearch, current: Long, size: Long): Map<String, Any> {
        val page = Page<StockIn>(current, size)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }
        val wrapper = MPJLambdaWrapper<StockIn>()
            .like(!search.no.isNullOrBlank(), StockIn::getReceiptNo, search.no)
            .eq(!search.locId.isNullOrBlank(), StockIn::getLocId, search.locId)
            .between(ProdOrder::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockIn::getId)

        val pageObj = plusService.page(page, wrapper)

        val dtoList = stockInMapStruct.toDtoList(pageObj.records)

        val ids = dtoList.map { it.id }
        val allStockInTags = stockInTagPlusService.listByPIds(ids)
        val tagNosByPId = allStockInTags.groupBy({ it.pId }, { it.tagNo })
        val allTagNos = allStockInTags.map { it.tagNo }
        val prodTagMap = prodTagPlusService.listByTagNos(allTagNos).associateBy { it.tagNo }

        dtoList.forEach { dtoIt ->
            val tagNos = tagNosByPId[dtoIt.id] ?: emptyList()
            dtoIt.tags = tagNos.mapNotNull { prodTagMap[it] }
        }

        return hashMapOf<String, Any>(
            "total" to pageObj.total,
            "records" to dtoList,
        )
    }
}
