package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
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
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition


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
    private val pmt: PlatformTransactionManager
) : BaseService() {

    /**
     * 新增托盘
     *
     * @param tagNos 纸箱标签号列表
     * @throws WebException 标签为空、不存在或已打包时抛出
     */
    fun add(tagNos: List<String>) : String{

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

       try {
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

           // 5. 保存打印日志
           // 说明（PAL-7）：托盘标签打印暂不生成 PDF 文件，此处仅记录打印日志（type=2）。
           // 若后续需要打印托盘标签，参照 ProdTagService.generateExcel 实现（需补充模板 tempId）。
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
           pmt.commit(status)
           log.info("托盘组托完成: palletNo={}, 标签数量={}, 净重={}, 毛重={}", pallet.palletNo, tags.size, pallet.netWeight, pallet.grossWeight)
           return pallet.palletNo
       }catch (e:Exception){
           pmt.rollback(status)
           throw WebException(e.message ?: "打托失败", e)
       }

    }

    /**
     * 拆托：从托盘中移除指定纸箱标签。
     *
     * 1. 校验托盘存在
     * 2. 校验所有待拆标签属于该托盘
     * 3. 软删除 PalletTag 关联记录
     * 4. 重新计算托盘汇总（数量/毛重/净重）；若全部拆完则软删除托盘
     *
     * @param palletNo 托盘号
     * @param tagNos 待拆除的纸箱标签号列表
     * @throws WebException 托盘不存在或标签不在托盘中时抛出
     */
    fun unbundle(palletNo: String, tagNos: List<String>) {
        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            // 1. 查询托盘
            val pallet = palletPlusService.getOne(
                QueryWrapper<Pallet>()
                    .eq("palletNo", palletNo)
                    .eq("deleted", 0)
            ) ?: throw WebException("【${palletNo}】该托盘不存在")

            // 2. 查询该托盘关联的所有标签
            val allTags = palletTagPlusService.listByPId(pallet.id)
            val existingTagNos = allTags.map { it.tagNo }.toSet()

            // 3. 校验：待拆标签必须全部属于该托盘
            val missing = tagNos.filter { it !in existingTagNos }
            if (missing.isNotEmpty()) {
                throw WebException("【${missing.joinToString(",")}】不在托盘${palletNo}中")
            }

            // 4. 软删除对应 PalletTag 记录
            LambdaUpdateChainWrapper(palletTagPlusService.baseMapper)
                .set(PalletTag::getDeleted, 1)
                .eq(PalletTag::getDeleted, 0)
                .eq(PalletTag::getPId, pallet.id)
                .`in`(PalletTag::getTagNo, tagNos)
                .update()

            // 5. 更新托盘汇总或软删除托盘
            val remainingTagNos = existingTagNos - tagNos.toSet()
            if (remainingTagNos.isEmpty()) {
                // 全部拆完 → 软删除托盘
                LambdaUpdateChainWrapper(palletPlusService.baseMapper)
                    .set(Pallet::getDeleted, 1)
                    .eq(Pallet::getId, pallet.id)
                    .update()
            } else {
                // 重新计算托盘汇总
                val resolved = billTagResolver.resolve(remainingTagNos.toList())
                pallet.qty = resolved.total.qty
                pallet.grossWeight = resolved.total.grossWeight
                pallet.netWeight = resolved.total.netWeight
                palletPlusService.updateById(pallet)
            }

            pmt.commit(status)
            log.info("托盘拆托/解绑完成: palletNo={}, 待拆标签数={}, 剩余标签数={}", palletNo, tagNos.size, remainingTagNos.size)
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message ?: "拆托失败", e)
        }
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
        // PAL-3：托盘展开场景下 flag=1（已打包校验）不适用——托盘内标签必然已打包，
        // 透传 flag=1 会导致全量误报"已打包"、整托操作永远失败；此处降级为 flag=0（仅查视图）。
        return tagNos.map { tagNo ->
            prodTagService.findVoByTagNo(tagNo, if (flag == 1) 0 else flag)
        }
    }
}
