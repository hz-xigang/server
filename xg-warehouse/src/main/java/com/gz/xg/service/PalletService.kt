package com.gz.xg.service

import com.gz.xg.UserContext
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

/**
 * 托盘服务，负责托盘主表和托盘标签关联的生成。
 */
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

    /**
     * 构建托盘主表数据。
     */
    override fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): Pallet {
        val pallet = Pallet()
        pallet.id = id
        pallet.palletNo = no
        pallet.qty = total.qty
        pallet.grossWeight = total.grossWeight
        pallet.netWeight = total.netWeight
        val (userId, username) = UserContext.require()
        pallet.userId = userId
        pallet.username = username

        return pallet
    }

    @Suppress("UNCHECKED_CAST")
    override fun saveBill(entity: Any) { palletPlusService.save(entity as Pallet) }

    /**
     * 构建托盘与纸箱标签的关联记录。
     */
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
