package com.gz.xg.service

import com.gz.xg.domain.dto.ProdTagTotal
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.ProdTagPlusService
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * 单据标签解析器
 *
 * 职责：标签去重、存在性校验、汇总计算
 * 不负责：占用检查、主表构建、保存操作
 */
@Component
class BillTagResolver(
    private val prodTagPlusService: ProdTagPlusService
) {

    /**
     * 解析并校验标签，返回标签详情和汇总结果
     *
     * @param tagNos 原始标签号列表
     * @return 解析结果，包含去重后的标签号、标签详情、汇总数据
     * @throws WebException 标签为空或不存在时抛出
     */
    fun resolve(tagNos: List<String>): ResolvedTags {
        if (tagNos.isEmpty()) {
            throw WebException("请扫描纸箱标签")
        }

        val distinctTagNos = tagNos.distinct()
        val prodTags = prodTagPlusService.listByTagNos(distinctTagNos)

        // 校验标签存在性
        if (prodTags.size != distinctTagNos.size) {
            val exists = prodTags.map { it.tagNo }.toSet()
            val missing = distinctTagNos - exists
            throw WebException("【${missing.joinToString(",")}】不存在")
        }

        // 汇总数量和重量
        val total = prodTags.fold(
            ProdTagTotal(0, BigDecimal.ZERO, BigDecimal.ZERO)
        ) { acc, item ->
            ProdTagTotal(
                acc.qty + item.qty,
                acc.grossWeight + item.grossWeight,
                acc.netWeight + item.netWeight
            )
        }

        return ResolvedTags(distinctTagNos, prodTags, total)
    }
}

/**
 * 标签解析结果
 *
 * @property tagNos 去重后的标签号列表
 * @property prodTags 标签详情列表
 * @property total 汇总数据（数量、毛重、净重）
 */
data class ResolvedTags(
    val tagNos: List<String>,
    val prodTags: List<VProdTag>,
    val total: ProdTagTotal
)
