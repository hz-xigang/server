package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.entity.TransferOrder
import com.gz.xg.domain.entity.TransferRecord
import com.gz.xg.domain.mapstruct.TransferRecordMapStruct
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.StockInTagPlusService
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
    private val sysSequenceService: SysSequenceService,
    private val locArchivePlusService: LocArchivePlusService,
    private val stockOutService: StockOutService,
    private val stockInService: StockInService,
) : BaseService() {

    fun add(addStockOrder: AddStockOrder): String {
        val order = transferOrderPlusService.findByNo(addStockOrder.no)
        val tagNos = addStockOrder.tagNos

        val records = arrayListOf<TransferRecord>()
        val prodTags = prodTagPlusService.listByTagNos(tagNos)
        val orderDetails = transferOrderDetailPlusService.listById(order.id)

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {

            var no = ""
            var loc  = LocArchive()

            if (order.orderType == 1){
                no = sysSequenceService.generateStockIn()
                loc = locArchivePlusService.byCode(order.toWarehouse)
            }else{
                no = sysSequenceService.generateStockOut()
                loc = locArchivePlusService.byCode(order.fromWarehouse)
            }


            prodTags.forEach { it ->
                run {
                    val detail = orderDetails.find { oIt -> oIt.inventoryCode == it.inventoryCode }
                        ?: throw WebException("【${it.tagNo}】存货编码【${it.inventoryCode}】不在调拨指令清单中")
                    val record = transferRecordMapStruct.detailToEntity(detail)
                    record.id = IdUtil.generateId()
                    record.transferOrderNo = order.orderNo
                    record.tagNo = it.tagNo
                    records.add(record)
                }
            }

            plusService.saveBatch(records)
            val u8ErrorMsg = if (order.orderType == 1){
                stockInService.addByTransfer(records,no,loc)
            }else{
                stockOutService.addByTransfer(records,no,loc)
            }
            pmt.commit(status)
            log.info("调拨确认成功: orderNo={}, orderType={}, 出入库单号={}, 条数={}", order.orderNo, order.orderType, no, records.size)
            return u8ErrorMsg
        }catch (e: Exception){
            pmt.rollback(status)
            throw WebException(e.message ?: "调拨失败", e)
        }

    }


}
