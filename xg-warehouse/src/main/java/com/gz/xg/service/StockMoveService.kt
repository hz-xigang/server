package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockInTag
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockMovePlusService
import com.gz.xg.service.plus.StockMoveTagPlusService
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager

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

        val (userId, username) = UserContext.require()
        move.userId = userId
        move.username = username

        return move
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveBill(entity: Any) {
        plusService.save(entity as StockMove)
    }

    /**
     * 构建移库单与标签的关联记录。
     */
    override fun buildTagEntry(pId: String, tagNo: String): TagEntity {
        val tag = StockMoveTag()
        tag.pId = pId
        tag.tagNo = tagNo
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
        val locArchive = locArchivePlusService.getById(locId) ?: throw WebException("该库位不存在")
        doAdd(tagNos, mutableMapOf("locId" to locArchive.id, "locCode" to locArchive.locCode)) { prodTags, _ ->
            run {
                stockInventoryService.editLoc(prodTags, locArchive)
            }
        }
    }
}
