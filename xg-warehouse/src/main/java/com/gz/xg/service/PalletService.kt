package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.gz.xg.UserContext
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.domain.entity.PrintLog
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PrintLogMapper
import com.gz.xg.service.plus.PalletPlusService
import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 托盘服务，负责托盘主表和托盘标签关联的生成。
 */
@Service
class PalletService(
    private val palletPlusService: PalletPlusService,
    private val palletTagPlusService: PalletTagPlusService,
    private val prodTagService: ProdTagService,
    private val sysSequenceService: SysSequenceService,
    private val billTagResolver: BillTagResolver,
    private val printLogMapper: PrintLogMapper,
    private val transactionManager: PlatformTransactionManager,
) {

    /**
     * 新增托盘
     *
     * @param tagNos 纸箱标签号列表
     * @throws WebException 标签为空、不存在或已打包时抛出
     */
    fun add(tagNos: List<String>) : String {
       return runCatching {
           TransactionTemplate(transactionManager).execute {
               // 1. 解析和校验标签
               val resolved = billTagResolver.resolve(tagNos)

               // 2. 检查是否已打包
               val occupied = palletTagPlusService.listByTagNos(resolved.tagNos)
               if (occupied.isNotEmpty()) {
                   throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已打包")
               }

               // 3. 构建托盘主表
               val genPalletNo =  sysSequenceService.generatePallet()
               val id = IdUtil.generateId()
               val (userId, username, realName) = UserContext.require()

               val pallet = Pallet().apply {
                   this.id = id
                   palletNo = sysSequenceService.generatePallet()
                   qty = resolved.total.qty
                   grossWeight = resolved.total.grossWeight
                   netWeight = resolved.total.netWeight
                   this.userId = userId
                   this.username = username
                   this.realName = realName
               }

               // 4. 构建托盘标签关联
               val tags = resolved.tagNos.map { tagNo ->
                   PalletTag().apply {
                       pId = id
                       this.tagNo = tagNo
                   }
               }

               // 5. 保存打印日志
               val printLog = PrintLog().apply {
                   this.id = IdUtil.generateId()
                   no = pallet.palletNo
                   type = 2
                   this.userId = userId
                   this.username = username
                   this.realName = realName
               }

               // 6. 保存
               palletPlusService.save(pallet)
               palletTagPlusService.saveBatch(tags)
               printLogMapper.insert(printLog)

               pallet.palletNo
           }
       }.getOrElse { "" }

    }

    /**
     * 根据托盘号查询关联的纸箱标签详情，并执行占用校验
     *
     * @param palletNo 托盘号
     * @param flag 校验标志位（1:已打包, 2:已入库, 7:在库存中）
     * @return 纸箱标签详情列表
     * @throws WebException 托盘不存在或标签校验失败时抛出
     */
    fun findTagsByPalletNo(palletNo: String, flag: Int): List<VProdTag> {
        // 1. 根据托盘号查询托盘主表
        val pallet = palletPlusService.getOne(
            QueryWrapper<Pallet>()
                .eq("palletNo", palletNo)
                .eq("deleted", 0)
        ) ?: throw WebException("托盘号【${palletNo}】不存在")

        // 2. 根据托盘 ID 查询所有关联的纸箱标签号
        val palletTags = palletTagPlusService.listByPId(pallet.id)
        if (palletTags.isEmpty()) {
            return emptyList()
        }

        val tagNos = palletTags.map { it.tagNo }

        // 3. 逐个查询纸箱标签详情，并执行占用校验
        return tagNos.map { tagNo ->
            prodTagService.findVoByTagNo(tagNo, flag)
        }
    }
}
