package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.domain.entity.Pallet
import com.gz.xg.domain.entity.PalletTag
import com.gz.xg.exception.WebException
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
    private val sysSequenceService: SysSequenceService,
    private val billTagResolver: BillTagResolver,
    private val transactionManager: PlatformTransactionManager,
) {

    /**
     * 新增托盘
     *
     * @param tagNos 纸箱标签号列表
     * @throws WebException 标签为空、不存在或已打包时抛出
     */
    fun add(tagNos: List<String>) {
        TransactionTemplate(transactionManager).executeWithoutResult {
        // 1. 解析和校验标签
        val resolved = billTagResolver.resolve(tagNos)

        // 2. 检查是否已打包
        val occupied = palletTagPlusService.listByTagNos(resolved.tagNos)
        if (occupied.isNotEmpty()) {
            throw WebException("【${occupied.joinToString(",") { it.tagNo }}】已打包")
        }

        // 3. 构建托盘主表
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

            // 5. 保存
            palletPlusService.save(pallet)
            palletTagPlusService.saveBatch(tags)
        }
    }
}
