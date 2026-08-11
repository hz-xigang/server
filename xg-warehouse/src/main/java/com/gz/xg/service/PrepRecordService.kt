package com.gz.xg.service

import com.gz.xg.domain.entity.PrepRecord
import com.gz.xg.domain.mapstruct.PrepRecordMapStruct
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.service.plus.PrepOrderDetailPlusService
import com.gz.xg.service.plus.PrepOrderPlusService
import com.gz.xg.service.plus.PrepRecordPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@Service
class PrepRecordService(
    private val plusService: PrepRecordPlusService,
    private val prepOrderPlusService: PrepOrderPlusService,
    private val prepOrderDetailPlusService: PrepOrderDetailPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val  pmt: PlatformTransactionManager,
    private val stockMoveService: StockMoveService,
    private val locArchivePlusService: LocArchivePlusService,
    private val prepRecordMapStruct: PrepRecordMapStruct
) {

    fun add(addStockOrder: AddStockOrder){
        val prepOrder = prepOrderPlusService.findByNo(addStockOrder.no)
        val tagNos = addStockOrder.tagNos
        val records = arrayListOf<PrepRecord>()

        val prodTags = prodTagPlusService.listByTagNos(tagNos)
        val orderDetails = prepOrderDetailPlusService.listByPId(prepOrder.id)

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            val packLoc = locArchivePlusService.packLoc()

            prodTags.forEach { it ->
                run {
                    val detail = orderDetails.find { oIt -> oIt.inventoryCode == it.inventoryCode }
                        ?: throw WebException("【${it.tagNo}】存货编码【${it.inventoryCode}】不在备料指令清单中")

                    // PRE-4：当前不做超量备料硬拦截。
                    // 原因：指令计划数量与实际备料数量可能存在差异（如指令备100箱，现场可能备150箱），
                    // 业务上允许超出，后续如需控制可设定允许超出的比例或绝对值。
                    val record = prepRecordMapStruct.detailToEntity(detail)
                    record.id = IdUtil.generateId()
                    record.prepOrderNo = prepOrder.prepNo
                    record.tagNo = it.tagNo
                    records.add(record)
                }
            }

            plusService.saveBatch(records)
            stockMoveService.addByPrep(records,packLoc)
            pmt.commit(status)
        }catch (e: Exception){
            pmt.rollback(status)
            throw WebException(e.message ?: "备料失败", e)
        }

    }

}
