package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.StockIn
import com.gz.xg.domain.entity.StockMove
import com.gz.xg.domain.entity.StockMoveTag
import com.gz.xg.domain.mapstruct.StockMoveMapStruct
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.service.plus.StockMovePlusService
import com.gz.xg.service.plus.StockMoveTagPlusService
import com.gz.xg.util.DateUtil
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
    private val prodTagPlusService: ProdTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
    private val mapStruct: StockMoveMapStruct,
    private val u8StockMoveSyncService: U8StockMoveSyncService
) : BaseService() {

    /**
     * 新增移库单，并同步修改库存库位。
     */
    /**
     * 新增移库单，并同步修改库存库位。返回 U8 同步错误信息（空字符串表示同步成功）。
     */
    fun add(req: AddStockIn): String {
        val u8Result = TransactionTemplate(transactionManager).execute {
            val locArchive = locArchivePlusService.getById(req.locId)
                ?: throw WebException("该库位不存在")

            if (locArchive.status == "禁用") {
                throw WebException("该库位已禁用")
            }

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
                type = "普通移库"
                this.userId = userId
                this.username = username
                this.realName = realName
            }

            val syncResult = u8StockMoveSyncService.syncStockMove(
                resolved, originLocMap, stockMove, locArchive
            )

            val tags = resolved.tagNos.map { tagNo ->
                val originStock = originLocMap[tagNo] ?: throw WebException("【${tagNo}】不在库存中")
                StockMoveTag().apply {
                    pId = id
                    this.tagNo = tagNo
                    oLocId = originStock.locId
                    oLocCode = originStock.locCode
                    u8Sync = syncResult.statusMap[tagNo] ?: 2
                }
            }

            plusService.save(stockMove)
            stockMoveTagPlusService.saveBatch(tags)
            stockInventoryService.editLoc(resolved.prodTags, locArchive)
            log.info("普通移库完成: receiptNo={}, 目标库位={}, 移动标签数={}", stockMove.receiptNo, locArchive.locCode, tags.size)
            syncResult.copy(failCount = tags.count { it.u8Sync == 0 })
        }
        return u8Result?.errorMessage ?: ""
    }

    /**
     * 备料移库。返回 U8 同步错误信息（空字符串表示同步成功）。
     */
    fun addByPrep(records: List<PrepRecord>, locArchive: LocArchive): String {
        val u8Result = TransactionTemplate(transactionManager).execute {
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

            val syncResult = u8StockMoveSyncService.syncStockMove(
                resolved, originLocMap, stockMove, locArchive
            )

            val tags = records.map {
                val originStock = originLocMap[it.tagNo] ?: throw WebException("【${it.tagNo}】不在库存中")
                StockMoveTag().apply {
                    pId = id
                    tagNo = it.tagNo
                    oLocId = originStock.locId
                    oLocCode = originStock.locCode
                    u8Sync = syncResult.statusMap[it.tagNo] ?: 2
                }
            }

            plusService.save(stockMove)
            stockMoveTagPlusService.saveBatch(tags)
            stockInventoryService.editLocByTagNo(tagNos, locArchive)
            log.info("备料移库完成: receiptNo={}, 目标库位={}, 移动标签数={}", stockMove.receiptNo, locArchive.locCode, tags.size)
            syncResult.copy(failCount = tags.count { it.u8Sync == 0 })
        }
        return u8Result?.errorMessage ?: ""
    }

    fun page(search: StockSearch,current: Long, size: Long): Map<String, Any> {
        val page = Page<StockMove>(current, size)
        DateUtil.initBaseSearch(search)
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }
        val wrapper = LambdaQueryWrapper<StockMove>()
            .like(!search.no.isNullOrBlank(), StockMove::getReceiptNo, search.no)
            .like(!search.locCode.isNullOrBlank(), StockMove::getLocCode, search.locCode)
            .between(StockMove::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockMove::getId)

        val pageObj = plusService.page(page, wrapper)
        val dtoList = mapStruct.toDtoList(pageObj.records)
        val ids = dtoList.map { it.id }
        if (ids.isNotEmpty()){
            val moveTags = stockMoveTagPlusService.listByPIds(ids)
            val tagNosByPId = moveTags.groupBy({ it.pId }, { it.tagNo })
            val allTagNos = moveTags.map { it.tagNo }
            val prodTagMap = prodTagPlusService.moveTagListByTagNos(allTagNos).associateBy { it.tagNo }

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
