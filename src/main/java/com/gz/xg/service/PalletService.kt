package com.gz.xg.service

import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.PalletPlusService
import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager

@Service
class PalletService(
    private val palletPlusService: PalletPlusService,
    private val palletTagPlusService: PalletTagPlusService,
    prodTagPlusService: ProdTagPlusService,
    pmt: PlatformTransactionManager,
    private val sysSequenceService: SysSequenceService,
) : AbstractBillService(prodTagPlusService, pmt) {

    override val tagOccupiedMessage = "已打包"

    override fun generateNo() = sysSequenceService.generatePallet()

    override fun tagService(): AbstractTagPlusService<*, *> = palletTagPlusService

    override fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): Pallet {
        val pallet = Pallet()
        pallet.id = id
        pallet.palletNo = no
        pallet.qty = total.qty
        pallet.grossWeight = total.grossWeight
        pallet.netWeight = total.netWeight
        pallet.printUser = "001"
        return pallet
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveBill(entity: Any) { palletPlusService.save(entity as Pallet) }

    override fun buildTagEntry(pId: String, tagNo: String): TagEntity {
        val tag = PalletTag()
        tag.pId = pId
        tag.tagNo = tagNo
        return tag
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveTagBatch(tags: List<TagEntity>) {
        palletTagPlusService.saveBatch(tags as List<PalletTag>)
    }
}
