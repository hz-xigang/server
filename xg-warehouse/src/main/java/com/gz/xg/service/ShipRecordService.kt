package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.domain.entity.ShipRecord
import com.gz.xg.domain.req.AddStockOrder
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.VProTagMapper
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ShipOrderDetailPlusService
import com.gz.xg.service.plus.ShipOrderPlusService
import com.gz.xg.service.plus.ShipRecordPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

@Service
class ShipRecordService(
    private val plusService: ShipRecordPlusService,
    private  val shipOrderPlusService: ShipOrderPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val shipOrderDetailPlusService: ShipOrderDetailPlusService,
    private val sysSequenceService: SysSequenceService,
    private val  pmt: PlatformTransactionManager,
    private val stockOutService: StockOutService
) {

    fun add(addStockOrder: AddStockOrder){

        val shipOrder = shipOrderPlusService.findByNo(addStockOrder.no)
        val tagNos = addStockOrder.tagNos
        val shipRecords = arrayListOf<ShipRecord>()

        val prodTags = prodTagPlusService.listByTagNos(tagNos)
        val orderDetails = shipOrderDetailPlusService.listByPId(shipOrder.id)


        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {

            // 1- 构建出货单
            val outNo = sysSequenceService.generateStockOut()

            prodTags.forEach { it ->
                run {
                    val record = ShipRecord()
                    record.id = IdUtil.generateId()
                    record.shipOrder = addStockOrder.no
                    val orderDetail = orderDetails.find { oIt -> oIt.inventoryCode == it.inventoryCode }
                    record.loc = orderDetail?.warehouseName
                    record.inventoryCode = it.inventoryCode
                    record.inventoryName = it.inventoryName
                    record.spec = it.spec
                    record.material = orderDetail?.material
                    record.unit = orderDetail?.unit
                    record.weight = orderDetail?.weight
                    record.qty = it.qty
                    record.unitWeight = orderDetail?.unitWeight
                    record.packingMethod = orderDetail?.packingMethod
                    record.specWidth = orderDetail?.specWidth
                    record.customerCode = it.customerCode
                    record.tagNo = it.tagNo
                    record.shipOrder = shipOrder.shipNo

                    shipRecords.add(record)
                }
            }
            plusService.saveBatch(shipRecords)
            stockOutService.addByShip(shipRecords  , outNo)
            pmt.commit(status)

        }catch (e: Exception){
            e.printStackTrace()
            pmt.rollback(status)
            throw WebException(e.message)
        }





    }

}
