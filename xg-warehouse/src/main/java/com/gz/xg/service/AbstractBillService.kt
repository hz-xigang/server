package com.gz.xg.service

import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.AbstractTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.math.BigDecimal

/**
 * 单据类服务的抽象父类，统一处理标签校验、汇总、主表保存和事务提交。
 */
abstract class AbstractBillService(
    val prodTagPlusService: ProdTagPlusService,
    private val pmt: PlatformTransactionManager,
    val checkExits : Boolean = true
) : BaseService(){

    /** 生成单据号。 */
    protected abstract fun generateNo(): String

    /** 根据汇总结果和上下文构建主表实体。 */
    protected abstract fun buildBill(id: String, no: String, total: ProdTagTotal, context: Map<String, Any>): Any

    /** 保存主表实体。 */
    protected abstract fun saveBill(entity: Any)

    /** 返回当前业务对应的标签关联服务。 */
    protected abstract fun tagService(): AbstractTagPlusService<*, *>

    /** 构建一条标签关联记录。 */
    protected abstract fun buildTagEntry(pId: String, tagNo: String): TagEntity

    /** 批量保存标签关联记录。 */
    protected abstract fun saveTagBatch(tags: List<TagEntity>)

    /** 标签已被占用时的提示文案，由子类自行定义。 */
    protected abstract val tagOccupiedMessage: String

    /**
     * 仅基于标签号生成单据的快捷入口。
     */
    fun add(tagNos: List<String>) {
        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")
        doAdd(tagNos, mutableMapOf())
    }

    /**
     * 通用的新增单据流程，支持附带业务上下文和额外落库动作。
     */
    protected fun doAdd(
        tagNos: List<String>, context: MutableMap<String, Any>,
        saveBusiness: (List<VProdTag>, ProdTagTotal) -> Unit = { _, _ -> }
    ) {
        if (tagNos.isEmpty()) throw WebException("请扫描纸箱标签")

        val distinctTagNos = tagNos.distinct()
        var errMsg = ""

        val prodTags = prodTagPlusService.listByTagNos(distinctTagNos)
        if (prodTags.isEmpty()) throw WebException("纸箱标签不存在")

        if (prodTags.size != distinctTagNos.size) {
            val exists = prodTags.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            errMsg += "【${missing.joinToString(",")}】不存在\r\n"
        }

        if (checkExits) {
            val occupiedTags = tagService().listByTagNos(distinctTagNos)
            if (occupiedTags.isNotEmpty()) {
                errMsg += "【${occupiedTags.joinToString(",") { it.tagNo }}】$tagOccupiedMessage"
            }
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
