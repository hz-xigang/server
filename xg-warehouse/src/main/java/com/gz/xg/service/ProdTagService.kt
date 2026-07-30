package com.gz.xg.service

import com.google.gson.Gson
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.search.ProdTagSearch
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.domain.entity.ProdOrder

import com.gz.xg.config.resource.StaticYmlConfig
import com.gz.xg.exception.WebException
import com.gz.xg.report.ProdTagReport
import com.gz.xg.service.plus.*
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 纸箱标签服务，负责标签生成、按生产单查询以及扫码校验。
 */
@Service
 class ProdTagService(
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService,
    private val prodTagMapStruct: ProdTagMapStruct,
    private val palletTagPlusService: PalletTagPlusService,
    private val stockInTagPlusService: StockInTagPlusService,
    private val stockInventoryPlusService: StockInventoryPlusService,
    private val prodOrderService: ProdOrderService,
    private val fileTempPlusService: FileTempPlusService,
    private val staticYmlConfig: StaticYmlConfig,
) : BaseService() {

    /**
     * 新增纸箱标签，生成标签号并打印标签。
     * @return PDF 字节数组（如果配置了模板），否则返回 null
     */
    fun add(dto: ProdTagDto): ByteArray? {
        val prodOrder = productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        val (userId, username,realName) = UserContext.require()
        prodTag.id = IdUtil.generateId()
        prodTag.userId = userId
        prodTag.username = username
        prodTag.realName = realName

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)

        // 生成打印标签 PDF
        return if (prodOrder.tempId != null) {
            generateExcel(prodTag, prodOrder)
        } else {
            null
        }
    }

    /**
     * 生成标签 PDF。
     * 流程：获取 tempId → 查询模板 path → 拼接完整路径 → 生成 PDF
     */
    private fun generateExcel(prodTag: ProdTag, prodOrder: ProdOrder): ByteArray {
        val fileTemp = fileTempPlusService.byId(prodOrder.tempId)

        if (fileTemp.path.isNullOrBlank()) {
            throw WebException("打印模板文件未上传")
        }

        val templatePath = staticYmlConfig.fullTemplate() + File.separator + fileTemp.path
        val data = prepareReportData(prodTag, prodOrder)

        return ProdTagReport().generate(templatePath, data)
    }

    /**
     * 准备报表数据 Map。
     */
    private fun prepareReportData(
        prodTag: ProdTag,
        prodOrder: ProdOrder
    ): Map<String, Any> {
        return mapOf(
            "客户编号" to (prodOrder.customerCode ?: ""),
            "工单号" to (prodOrder.prodNo ?: ""),
            "客户订单号" to (prodOrder.erpOrderNo ?: ""),
            "产品类别" to (prodOrder.productCategory ?: ""),
            "规格型号" to (prodOrder.spec ?: ""),
            "数量" to prodTag.qty.toString(),
            "毛重" to prodTag.grossWeight.toString(),
            "净重" to prodTag.netWeight.toString(),
            "存货编码" to (prodOrder.inventoryCode ?: ""),
            "日期" to LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "生产单号" to prodTag.tagNo
        )
    }

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listByProdNo(search: ProdTagSearch) : List<VProdTag> {
       return prodTagPlusService.listVo(search)
    }

    /**
     * 按标签号查询标签详情，并根据标志位执行占用校验。
     */
    fun findVoByTagNo(tagNo: String, flag: Int): VProdTag {
        when (flag) {
            1 -> palletTagPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已打包")
            2 -> stockInTagPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已入库")
            7-> stockInventoryPlusService.assertNotExists(tagNo)
        }

        return when(flag) {
            3-> stockInTagPlusService.findVoByTagNo(tagNo)
            else -> prodTagPlusService.findVoByTagNo(tagNo)
        }
    }

    fun softDelById(id : String){
        prodTagPlusService.findById(id)
        changeDel(prodTagPlusService.baseMapper,
            ProdTag::getDeleted,1)
        {
            eq(ProdTag::getId,id)
        }
    }

}
