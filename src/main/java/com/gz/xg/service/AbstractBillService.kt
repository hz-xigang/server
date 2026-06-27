package com.gz.xg.service

import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal

abstract class AbstractBillService(
    val prodTagPlusService: ProdTagPlusService,
    private val pmt: PlatformTransactionManager,
) : BaseService(){

    /** 生成单据号 */
    protected abstract fun generateNo(): String

    /** 根据聚合结果和上下文构建主表实体 */
    protected abstract fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): Any

    /** 保存主表实体 */
    protected abstract fun saveBill(entity: Any)

    /** 返回当前业务对应的 Tag PlusService */
    protected abstract fun tagService(): AbstractTagPlusService<*, *>

    /** 构建一条 tag 关联记录 */
    protected abstract fun buildTagEntry(pId: String, tagNo: String): TagEntity

    /** 批量保存 tag 关联记录 */
    protected abstract fun saveTagBatch(tags: List<TagEntity>)

    /** 已占用时的错误提示文案，子类按语义覆盖 */
    protected abstract val tagOccupiedMessage: String

    fun add(tagNos: List<String>) {
        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        doAdd(tagNos, mutableMapOf())
    }

    protected fun doAdd(
        tagNos: List<String>, context: MutableMap<String, Any>,
        saveBusiness: (List<ProdTagVo>, ProdTagTotal) -> Unit = { _, _ -> }
    ) {
        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")

        val distinctTagNos = tagNos.distinct()
        var errMsg = ""

        val prodTags = prodTagPlusService.listByTagNos(distinctTagNos)
        if (prodTags.isEmpty()) throw WebException("纸箱标签不存在")

        if (prodTags.size != distinctTagNos.size) {
            val exists = prodTags.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            errMsg += "【${missing.joinToString(",")}】 不存在 \r\n"
        }

        val occupiedTags = tagService().listByTagNos(distinctTagNos)
        if (occupiedTags.isNotEmpty()) {
            errMsg += "【${occupiedTags.joinToString(",") { it.tagNo }}】 $tagOccupiedMessage"
        }

        if (errMsg.isNotEmpty()) throw WebException(errMsg)

        val total = prodTags.fold(ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)) { acc, item ->
            ProdTagTotal(
                acc.qty + item.qty,
                acc.grossWeight + item.grossWeight,
                acc.netWeight + item.netWeight
            )
        }

        val definition = DefaultTransactionDefinition()
        definition.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = pmt.getTransaction(definition)

        try {
            val id = IdUtil.generateId()
            val no = generateNo()

            saveBill(buildBill(id, no, total, context))

            val tagEntries = distinctTagNos.map { buildTagEntry(id, it) }
            saveTagBatch(tagEntries)

            saveBusiness(prodTags,total)

            pmt.commit(status)
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message)
        }
    }
}
