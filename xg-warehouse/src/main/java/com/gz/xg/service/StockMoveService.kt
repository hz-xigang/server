package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.domain.entity.StockInventory
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.domain.entity.StockOutTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.service.plus.StockMovePlusService
import com.gz.xg.service.plus.StockMoveTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import java.math.BigDecimal

/**
 * 移库服务，负责移库单生成以及库存库位更新。
 */
@Service
class StockMoveService(
    private val plusService: StockMovePlusService,
    prodTagPlusService: ProdTagPlusService,
    pmt: PlatformTransactionManager,
    private val sysSequenceService: SysSequenceService,
    private val stockMovePlusService: StockMovePlusService,
    private val stockMoveTagPlusService: StockMoveTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockInventoryService: StockInventoryService
): AbstractBillService(prodTagPlusService, pmt,false) {

    override fun generateNo() = sysSequenceService.generateMoves()

    override fun tagService(): AbstractTagPlusService<*, *> = stockMovePlusService

    /**
     * 构建移库单主表。
     */
    override fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): StockMove {
        val move = StockMove()
        move.id = id
        move.receiptNo = no
        move.qty = total.qty
        move.grossWeight = total.grossWeight
        move.netWeight = total.netWeight
        move.locId = context["locId"] as String
        move.locCode = context["locCode"] as String

        val (userId, username,realName) = UserContext.require()
        move.userId = userId
        move.username = username
        move.realName = realName

        return move
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveBill(entity: Any) {
        plusService.save(entity as StockMove)
    }

    /**
     * 构建移库单与标签的关联记录。
     */
    override fun buildTagEntry(pId: String, tagNo: String, context : Map<String, Any>): TagEntity {
        @Suppress("UNCHECKED_CAST")
        val originLocMap = context["originLocMap"] as Map<String, StockInventory>
        val originStock = originLocMap[tagNo] ?: throw WebException("【${tagNo}】不在库存中")

        val tag = StockMoveTag()
        tag.pId = pId
        tag.tagNo = tagNo
        tag.oLocId = originStock.locId
        tag.oLocCode = originStock.locCode
        return tag
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveTagBatch(tags: List<TagEntity>) {
        stockMoveTagPlusService.saveBatch(tags as List<StockMoveTag>)
    }

    override val tagOccupiedMessage = ""

    /**
     * 新增移库单，并同步修改库存库位。
     */
    fun add(req: AddStockIn) {
        val locId = req.locId
        val tagNos = req.tagNos

        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        val distinctTagNos = tagNos.distinct()
        val locArchive = locArchivePlusService.getById(locId) ?: throw WebException("该库位不存在")
        val originStocks = stockInventoryPlusService.listByTagNos(distinctTagNos)
        if (originStocks.size != distinctTagNos.size) {
            val exists = originStocks.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            throw WebException("【${missing.joinToString(",")}】不在库存中")
        }

        val context = mutableMapOf<String, Any>(
            "locId" to locArchive.id,
            "locCode" to locArchive.locCode,
            "originLocMap" to originStocks.associateBy { it.tagNo }
        )

        doAdd(tagNos, context) { prodTags, _ ->
            run {
                stockInventoryService.editLoc(prodTags, locArchive)
            }
        }
    }

    fun addByPrep(records : List<PrepRecord>, locArchive: LocArchive){
        val tagNos = records.map { it.tagNo }
        val total = calcTotal(tagNos)
        val id = IdUtil.generateId()
        val distinctTagNos = tagNos.distinct()
        val originStocks = stockInventoryPlusService.listByTagNos(distinctTagNos)
        if (originStocks.size != distinctTagNos.size) {
            val exists = originStocks.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            throw WebException("【${missing.joinToString(",")}】不在库存中")
        }
        val originLocMap = originStocks.associateBy { it.tagNo }

        val moveNo = sysSequenceService.generateMoves()
        val (userId, username,realName) = UserContext.require()
        val stockMove = StockMove().apply {
            this.id = id
            receiptNo = moveNo
            qty = total.qty
            grossWeight = total.grossWeight
            netWeight = total.netWeight
            this.userId = userId
            this.username = username
            this.realName = realName
            type = "备料移库"
            locCode = locArchive.locCode
            locId = locArchive.id
        }

        val tags = records.map {
            val originStock = originLocMap[it.tagNo] ?: throw WebException("【${it.tagNo}】不在库存中")
            StockMoveTag().apply {
                pId = id
                tagNo = it.tagNo
                oLocId = originStock.locId
                oLocCode = originStock.locCode
            }
        }

        plusService.save(stockMove)
        stockMoveTagPlusService.saveBatch(tags)
        stockInventoryService.editLocByTagNo(tagNos, locArchive)

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

}
