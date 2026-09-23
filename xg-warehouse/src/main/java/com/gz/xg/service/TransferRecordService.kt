package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.TransferRecordMapStruct
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.TransferOrderDetailPlusService
import com.gz.xg.service.plus.TransferOrderPlusService
import com.gz.xg.service.plus.TransferRecordPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@Service
class TransferRecordService(
    private val plusService: TransferRecordPlusService,
    private val transferOrderPlusService: TransferOrderPlusService,
    private val transferOrderDetailPlusService: TransferOrderDetailPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val transferRecordMapStruct: TransferRecordMapStruct,
    private val pmt: PlatformTransactionManager,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockMoveService: StockMoveService
) : BaseService() {

    fun add(addStockOrder: AddStockOrder): String {
        val targetLocCode = addStockOrder.locCode
        if (targetLocCode.isBlank()) {
            throw WebException("目标库位不能为空")
        }

        val targetLoc = locArchivePlusService.byCode(targetLocCode)

        val order = transferOrderPlusService.findByNo(addStockOrder.no)
        val tagNos = addStockOrder.tagNos
        if (tagNos.isEmpty()) {
            throw WebException("调拨标签列表不能为空")
        }

        val records = arrayListOf<TransferRecord>()
        val prodTags = prodTagPlusService.listByTagNos(tagNos)
        val orderDetails = transferOrderDetailPlusService.listById(order.id)

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            prodTags.forEach { it ->
                val detail = orderDetails.find { oIt -> oIt.inventoryCode == it.inventoryCode }
                    ?: throw WebException("【${it.tagNo}】存货编码【${it.inventoryCode}】不在调拨指令清单中")
                val record = transferRecordMapStruct.detailToEntity(detail)
                record.id = IdUtil.generateId()
                record.transferOrderNo = order.orderNo
                record.tagNo = it.tagNo
                records.add(record)
            }

            plusService.saveBatch(records)

            // 执行移库并回传 U8 货位调整单，失败则整体回滚
            val u8ErrorMsg = stockMoveService.addByTransfer(records, targetLoc, order.orderNo)

            pmt.commit(status)
            log.info("调拨移库确认成功: orderNo={}, 目标库位={}, 条数={}", order.orderNo, targetLoc.locCode, records.size)
            return u8ErrorMsg
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message ?: "调拨失败", e)
        }
    }

}
