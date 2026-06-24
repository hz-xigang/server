package com.gz.xg.service

import com.google.gson.Gson
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.PalletPlusService
import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal


@Service
class PalletService(
    private val palletPlusService: PalletPlusService,
    private val palletTagPlusService: PalletTagPlusService,
    private val prodTagPlusService: ProdTagPlusService,
    private val pmt: PlatformTransactionManager,
    private val sysSequenceService: SysSequenceService
) {

    fun add(tagNos: List<String>) {
        if (tagNos.isEmpty()) {
            throw WebException("请扫描纸箱标签")
        }
        val distinctTagNos = tagNos.distinct()
        var errMfg = "";
        //1 -判断条码是否都存在
        val byTagNos = prodTagPlusService.listByTagNos(distinctTagNos)
        if (byTagNos.isEmpty()) {
            throw WebException("纸箱标签不存在")
        }


        if (byTagNos.size != distinctTagNos.size) {
            val map = byTagNos.map { it.tagNo }
            val notExitsTag = distinctTagNos - map.toSet()
            errMfg += "【${notExitsTag.joinToString(",")}】 不存在 \r\n"
        }

        // 2- 判断条码是否打包
        val palletTags = palletTagPlusService.listByTagNos(tagNos)

        if (palletTags.isNotEmpty()) {
            val palletTagNos = palletTags.map { it.tagNo }
            errMfg += "【${palletTagNos.joinToString(",")}】 已打包"
        }
        if (errMfg.isNotEmpty()) {
            throw WebException(errMfg)
        }

        // 3- 插入打包
        val definition = DefaultTransactionDefinition()

        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED

        val status = pmt.getTransaction(definition)

        try {
            val palletNo = sysSequenceService.generatePallet()
            val palletId = IdUtil.generateId()

            // 计算总件数 总重量
            val total = byTagNos.fold(
                ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)
            ) { acc, item ->
                ProdTagTotal(
                    acc.qty + item.qty,
                    acc.grossWeight + item.grossWeight,
                    acc.netWeight + item.netWeight
                )
            }

            // 保存主表
            val pallet = Pallet()
            pallet.id = palletId
            pallet.palletNo = palletNo
            pallet.qty = total.qty
            pallet.netWeight = total.netWeight
            pallet.grossWeight = total.grossWeight
            pallet.printUser = "001"

            palletPlusService.save(pallet)

            // 保存关系表
            val palletTags = arrayListOf<PalletTag>()
            distinctTagNos.forEach { it ->
                run {
                    val palletTag = PalletTag()
                    palletTag.pId = palletId
                    palletTag.tagNo = it
                    palletTags.add(palletTag)
                }
            }
            palletTagPlusService.saveBatch(palletTags)

            pmt.commit(status)


        } catch (e: Exception) {
            // 回滚事务
            pmt.rollback(status);
            throw WebException(e.message);
        }

    }


}



