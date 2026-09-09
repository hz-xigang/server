package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.domain.entity.StockOut
import com.gz.xg.domain.entity.StockOutTag
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.StockOutMapStruct
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
import com.gz.xg.service.plus.StockOutPlusService
import com.gz.xg.service.plus.StockOutTagPlusService
import com.gz.xg.util.DateUtil
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 出库单服务。
 *
 * 负责两类出库业务：
 * 1. **发货出库**（[addByShip]）：按发货单指令确认出库，同时推送用友 U8 成品出仓（销售出库）单；
 *    出库货位以库存表（StockInventory）实时位置为准，不再依赖指令单上的库位名称。
 * 2. **调拨出库**（[addByTransfer]）：按调拨单出库，目标货位由调用方传入，不推送 U8。
 *
 * 公共约束（SHP-2）：
 * - 出库前必须校验所有标签当前在库（StockInventory.deleted=0），不在库的一律阻止出库；
 * - 出库成功后会将对应库存记录软删除（deleted=1），完成库存核销；
 * - 出库单主表 [StockOut] + 明细 [StockOutTag] + 库存核销在同一个事务内完成，
 *   任意一步失败整体回滚，保证数据一致性。
 */
@Service
class StockOutService(
    private val plusService: StockOutPlusService,
    private val stockOutTagPlusService: StockOutTagPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val stockOutMapStruct: StockOutMapStruct,
    private val stockInventoryService: StockInventoryService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
    private val u8SalesStockOutSyncService: U8SalesStockOutSyncService
) : BaseService() {

    /**
     * 发货出库。
     *
     * 由发货确认流程（[ShipRecordService]）调用：将已核销的发货记录（records）生成出库单，
     * 并推送 U8 成品出仓单。
     *
     * 流程概览：
     * 1. 解析并去重标签（[BillTagResolver.resolve]），校验标签存在；
     * 2. SHP-2 库存校验：所有标签必须当前在库，缺失则阻止出库；
     * 3. 以库存实时货位（StockInventory.locCode/locId）为准，校验货位信息完整；
     * 4. 组装出库主单 [StockOut] 与明细 [StockOutTag]；
     * 5. 推送 U8 成品出仓单，将同步状态写入明细（u8Sync：1 成功 / 0 失败 / 2 不需同步）；
     * 6. 保存主单、批量保存明细、软删除库存记录。
     *
     * @param records 已核销的发货记录列表（每条包含 tagNo）
     * @param outNo 出库单号（由调用方生成，如 SHP 单号）
     * @param shipOrder 发货单主表，用于构建 U8 推送明细（客户、备注等）
     * @param orderDetails 发货单明细列表，U8 明细以发货单数据为准（存货编码、规格等）
     * @return U8 同步错误信息：空字符串表示全部同步成功；非空表示存在失败项，透传给前端展示
     */
    fun addByShip(
        records: List<ShipRecord>,
        outNo: String,
        shipOrder: com.gz.xg.domain.entity.ShipOrder,
        orderDetails: List<com.gz.xg.domain.entity.ShipOrderDetail>
    ): String {
        // 编程式事务：出库单 + 明细 + 库存核销 + 出库位校验，任一步失败整体回滚
        val u8Result = TransactionTemplate(transactionManager).execute {
            val tagNos = records.map { it.tagNo }

            // 标签解析：去重、校验标签在 ProdTag 中真实存在、汇总数量/毛重/净重
            val resolved = billTagResolver.resolve(tagNos)

            // SHP-2：校验所有标签是否在库（StockInventory.deleted=0）
            // 数量不一致说明存在未入库/已出库的标签，列出缺失项阻止出库
            val inventories = stockInventoryPlusService.listByTagNos(resolved.tagNos)
            if (inventories.size != resolved.tagNos.size) {
                val inStock = inventories.map { it.tagNo }.toSet()
                val missing = resolved.tagNos - inStock
                throw WebException("【${missing.joinToString(",")}】不在库存中，无法出库")
            }

            // 出库货位以库存实时位置为准（入库/移库时落库，天然来自有效库位档案）
            // 兜底校验：防御历史脏数据导致的 locCode/locId 为空
            val inventoryByTagNo = inventories.associateBy { it.tagNo }
            inventoryByTagNo.forEach { (tagNo, inv) ->
                if (inv.locCode.isNullOrBlank() || inv.locId.isNullOrBlank()) {
                    throw WebException("【$tagNo】库存库位信息缺失，无法出库")
                }
            }

            val id = IdUtil.generateId()
            val (userId, username, realName) = UserContext.require()

            // 组装出库主单：数量与重量取标签汇总值，货位取库存中不重复的 locCode
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
                loc = inventoryByTagNo.values.map { it.locCode }.distinct().joinToString(",")
            }

            // 组装出库明细：每条记录对应一条明细，货位直接取库存中的 locCode/locId
            val tags = records.map { record ->
                val inv = inventoryByTagNo[record.tagNo]
                    ?: throw WebException("【${record.tagNo}】不在库存中，无法出库")
                StockOutTag().apply {
                    pId = id
                    tagNo = record.tagNo
                    locCode = inv.locCode
                    locId = inv.locId
                }
            }

            // 推送 U8 成品出仓单：仅推送存货编码能匹配发货单明细的标签，
            // 返回每个 tagNo 的同步状态（statusMap）与错误信息
            val syncResult = u8SalesStockOutSyncService.syncSalesStockOut(
                resolved, stockOut, shipOrder, orderDetails,
                inventoryByTagNo.mapValues { it.value.locCode },
                inventoryByTagNo.mapValues { it.value.u8Batch ?: "" }
            )
            // 将 U8 同步状态回写到出库明细，供前端展示与后续补偿查询
            tags.forEach { it.u8Sync = syncResult.statusMap[it.tagNo] ?: 2 }

            plusService.save(stockOut)
            stockOutTagPlusService.saveBatch(tags)
            // 库存核销：软删除（deleted=1），完成出库
            stockInventoryService.changeDelByTagNos(tagNos)
            log.info("发货生成出库单完成: receiptNo={}, 库位={}, 标签数量={}", outNo, stockOut.loc, tags.size)
            // 以实际落库的明细状态重新统计失败数，返回给调用方透传
            syncResult.copy(failCount = tags.count { it.u8Sync == 0 })
        }
        // 空串表示同步成功；非空则透传 U8 错误信息给前端
        return u8Result?.errorMessage ?: ""
    }

    /**
     * 分页查询出库单列表。
     *
     * 支持按出库单号、货位模糊查询，并按创建时间区间过滤、按 id 倒序排列。
     * 查询出的每条出库单会附带其标签明细（tags），供前端展示纸箱标签信息。
     *
     * @param search 查询条件（no 出库单号 / locCode 货位 / startDate、endDate 时间区间）
     * @param current 当前页码
     * @param size 每页条数
     * @return 分页结果 Map：total 总条数，records 出库单 DTO 列表（含 tags 明细）
     */
    fun page(search: StockSearch, current: Long, size: Long) : Map<String, Any>  {
        val page = Page<StockOut>(current, size)
        // 结束日期加一天，使 between 查询包含当天（闭区间）
        search.endDate = search.endDate?.let { DateUtil.strAddDays(it) }

        // 组装查询条件：单号/货位模糊匹配 + 创建时间区间 + id 倒序
        val wrapper = MPJLambdaWrapper<StockOut>()
            .like(!search.no.isNullOrBlank(), StockOut::getReceiptNo, search.no)
            .like(!search.locCode.isNullOrBlank(), StockOut::getLoc, search.locCode)
            .between(StockOut::getCreateTime, search.startDate, search.endDate)
            .orderByDesc(StockOut::getId)

        val pageObj = plusService.page(page, wrapper)
        val dtoList = stockOutMapStruct.toDtoList(pageObj.records)
        val ids = dtoList.map { it.id }
        // 批量加载标签明细，避免 N+1 查询
        if (ids.isNotEmpty()){
            // 按出库单 id 分组查询明细中的 tagNo
            val allStockInTags = stockOutTagPlusService.listByPIds(ids)
            val tagNosByPId = allStockInTags.groupBy({ it.pId }, { it.tagNo })
            val allTagNos = allStockInTags.map { it.tagNo }
            // 一次性查询全部标签详情，建立 tagNo -> ProdTag 映射
            val prodTagMap = prodTagPlusService.listByTagNos(allTagNos).associateBy { it.tagNo }

            // 回填每条出库单的标签列表
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
