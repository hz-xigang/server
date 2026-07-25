package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.service.plus.StockMovePlusService
import com.gz.xg.service.plus.StockMoveTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 移库服务，负责移库单生成以及库存库位更新。
 */
@Service
class StockMoveService(
    private val plusService: StockMovePlusService,
    private val sysSequenceService: SysSequenceService,
    private val stockMoveTagPlusService: StockMoveTagPlusService,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
) {

    /**
     * 新增移库单，并同步修改库存库位。
     */
    fun add(req: AddStockIn) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val locArchive = locArchivePlusService.getById(req.locId)
                ?: throw WebException("该库位不存在")

            val resolved = billTagResolver.resolve(req.tagNos)

            // 校验标签是否在库存中
            val originStocks = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (originStocks.size != resolved.tagNos.size) {
                val exists = originStocks.map { it.tagNo }.toSet()
                val missing = resolved.tagNos - exists
                throw WebException("【${missing.joinToString(",")}】不在库存中")
            }

            val originLocMap = originStocks.associateBy { it.tagNo }
            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockMove = StockMove().apply {
                this.id = id
                receiptNo = sysSequenceService.generateMoves()
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
                locId = locArchive.id
                locCode = locArchive.locCode
                this.userId = userId
                this.username = username
                this.realName = realName
            }

            val tags = resolved.tagNos.map { tagNo ->
                val originStock = originLocMap[tagNo] ?: throw WebException("【${tagNo}】不在库存中")
                StockMoveTag().apply {
                    pId = id
                    this.tagNo = tagNo
                    oLocId = originStock.locId
                    oLocCode = originStock.locCode
                }
            }

            plusService.save(stockMove)
            stockMoveTagPlusService.saveBatch(tags)
            stockInventoryService.editLoc(resolved.prodTags, locArchive)
        }
    }

    /**
     * 备料移库
     */
    fun addByPrep(records: List<PrepRecord>, locArchive: LocArchive) {
        TransactionTemplate(transactionManager).executeWithoutResult {
            val tagNos = records.map { it.tagNo }
            val resolved = billTagResolver.resolve(tagNos)

            // 校验标签是否在库存中
            val originStocks = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (originStocks.size != resolved.tagNos.size) {
                val exists = originStocks.map { it.tagNo }.toSet()
                val missing = resolved.tagNos - exists
                throw WebException("【${missing.joinToString(",")}】不在库存中")
            }

            val originLocMap = originStocks.associateBy { it.tagNo }
            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            val stockMove = StockMove().apply {
                this.id = id
                receiptNo = sysSequenceService.generateMoves()
                qty = resolved.total.qty
                grossWeight = resolved.total.grossWeight
                netWeight = resolved.total.netWeight
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
    }

}
