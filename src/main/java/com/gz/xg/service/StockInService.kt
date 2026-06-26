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
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.util.DateUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager

@Service
open class StockInService(
    private val plusService: StockInPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    prodTagPlusService: ProdTagPlusService,
    pmt: PlatformTransactionManager,
    private val sysSequenceService: SysSequenceService,
    private val stockInMapStruct: StockInMapStruct
) : AbstractBillService(prodTagPlusService, pmt) {

    override val tagOccupiedMessage = "已入库"

    override fun generateNo() = sysSequenceService.generateStockIn()

    override fun tagService(): AbstractTagPlusService<*, *> = stockInTagPlusService

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

    fun add(req: AddStockIn) {
        val locId = req.locId
        val tagNos = req.tagNos

        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        val locArchive = locArchivePlusService.getById(locId) ?: throw WebException("该库位不存在")
        doAdd(tagNos, mutableMapOf("locId" to locArchive.id, "locCode" to locArchive.locCode))
    }

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

        dtoList.forEach {
            val tags = stockInTagPlusService.listByPId(it.id)

            prodTagPlusService.listByTagNos(tags.map { it -> it.tagNo })

        }




        return getDtoPage<StockIn, StockInDto>(pageObj) {
            stockInMapStruct.toDtoList(it)
        }
    }


}
