package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.config.resource.StaticYmlConfig
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.entity.PrintLog
import com.gz.xg.domain.entity.ProdOrder
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.search.ProdTagSearch
import com.gz.xg.domain.view.VProdTag
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PrintLogMapper
import com.gz.xg.report.ProdTagReport
import com.gz.xg.service.plus.*
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
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
    private val pmt: PlatformTransactionManager,
    private val printLogMapper: PrintLogMapper,
    private val shipOrderPlusService: ShipOrderPlusService,
    private val shipOrderDetailPlusService: ShipOrderDetailPlusService,
    private val transferOrderPlusService: TransferOrderPlusService,
    private val transferOrderDetailPlusService: TransferOrderDetailPlusService,
    private val prepOrderPlusService: PrepOrderPlusService,
    private val prepOrderDetailPlusService: PrepOrderDetailPlusService
) : BaseService() {

    /**
     * 新增纸箱标签，生成标签号并打印标签。
     * @return PDF 字节数组（如果配置了模板），否则返回 null
     */
    fun add(dto: ProdTagDto): ByteArray? {
        val def = DefaultTransactionDefinition().apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        }
        val status = pmt.getTransaction(def)

        try {
            val prodOrder = productionOrderPlusService.findById(dto.prodOrderId)
                ?: throw WebException("关联的生产单不存在")
            val prodTag = prodTagMapStruct.toEntity(dto)

            val userInfo = UserContext.require()

            val tagNo = sysSequenceService.generateCarton()
            prodTag.id = IdUtil.generateId()
            prodTag.userId = userInfo.userId
            prodTag.username = userInfo.username
            prodTag.realName = userInfo.realName
            prodTag.tagNo = tagNo
            prodTag.createTime = LocalDateTime.now()
            prodTag.deleted = 0
            prodTagPlusService.save(prodTag)

            val printLog = PrintLog().apply {
                id = IdUtil.generateId()
                no = tagNo
                type = 1
                tempId = prodOrder.tempId
                userId = userInfo.userId
                username = userInfo.username
                realName = userInfo.realName
            }
            printLogMapper.insert(printLog)

            pmt.commit(status)
            log.info("生成纸箱标签成功: prodNo={}, tagNo={}, qty={}, userId={}", prodOrder.prodNo, tagNo, dto.qty, userInfo.userId)

            if (prodOrder.tempId != null) {
                return generateExcel(prodTag, prodOrder)
            }

            return null
        } catch (e: Exception) {
            pmt.rollback(status)
            throw WebException(e.message)
        }
    }

    /**
     * 生成标签 PDF。
     * 流程：获取 tempId → 查询模板 path → 拼接完整路径 → 生成 PDF
     */
    private fun generateExcel(prodTag: ProdTag, prodOrder: ProdOrder): ByteArray {
        val fileTemp = fileTempPlusService.byId(prodOrder.tempId)

        if (fileTemp?.path.isNullTransOrBlank()) {
            throw WebException("打印模板文件未上传")
        }

        val templatePath = "${staticYmlConfig.fullTemplate()}${File.separator}${fileTemp.path}"
        System.err.println("templatePath@@$templatePath")
        val data = prepareReportData(prodTag, prodOrder)

        return ProdTagReport().generate(templatePath, data)
    }

    private fun CharSequence?.isNullTransOrBlank(): Boolean = this == null || this.isBlank()

    /**
     * 准备报表数据 Map。
     */
    private fun prepareReportData(prodTag: ProdTag, prodOrder: ProdOrder): Map<String, Any> {
        val data = HashMap<String, Any>()
        data["客户编号"] = prodOrder.customerCode ?: ""
        data["工单号"] = prodOrder.prodNo ?: ""
        data["客户订单号"] = prodOrder.erpOrderNo ?: ""
        data["产品类别"] = prodOrder.productCategory ?: ""
        data["规格型号"] = prodOrder.spec ?: ""
        data["数量"] = prodTag.qty.toString()
        data["毛重"] = prodTag.grossWeight.toString()
        data["净重"] = prodTag.netWeight.toString()
        data["存货编码"] = prodOrder.inventoryCode ?: ""
        data["日期"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        data["生产单号"] = prodTag.tagNo ?: ""
        return data
    }

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listByProdNo(search: ProdTagSearch): List<VProdTag> {
        return prodTagPlusService.listVo(search)
    }

    /**
     * 分页查询标签视图。
     */
    fun page(search: ProdTagSearch, current: Long, size: Long): Map<String, Any> {
        val pageObj = prodTagPlusService.page(search, current, size)
        val result = HashMap<String, Any>()
        result["total"] = pageObj.total
        result["records"] = pageObj.records
        return result
    }

    /**
     * 按标签号查询标签详情，并根据标志位执行占用校验。
     */
    fun findVoByTagNo(tagNo: String, flag: Int): VProdTag {
        when (flag) {
            1 -> palletTagPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已打包")
            2 -> stockInventoryPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已入库")
            7 -> stockInventoryPlusService.assertNotExists(tagNo)
        }

        val result = if (flag == 3) {
            stockInTagPlusService.findVoByTagNo(tagNo)
        } else {
            prodTagPlusService.findVoByTagNo(tagNo)
        }
        return result ?: throw WebException("【${tagNo}】纸箱标签不存在")
    }

    /**
     * 检查条码对应的存货编码是否在发货指令单明细中
     *
     * @param shipNo 发货指令单号
     * @param tagNo  纸箱条码
     * @return 标签视图对象
     */
    fun checkShipTag(shipNo: String, tagNo: String): VProdTag {
        val shipOrder = shipOrderPlusService.findByNo(shipNo)
            ?: throw WebException("发货指令单【${shipNo}】不存在")


        val tag = prodTagPlusService.findVoByTagNo(tagNo)
            ?: throw WebException("条码【${tagNo}】不存在")

        val details = shipOrderDetailPlusService.listByPId(shipOrder.id)
        val matched = details.any { d -> d.inventoryCode != null && d.inventoryCode == tag.inventoryCode }

        if (!matched) {
            throw WebException("【${tagNo}】存货编码【${tag.inventoryCode}】不在发货指令清单中")
        }

        return tag
    }

    /**
     * 检查条码对应的存货编码是否在调拨指令单明细中
     *
     * @param orderNo 调拨指令单号
     * @param tagNo   纸箱条码
     * @return 标签视图对象
     */
    fun checkTransferTag(orderNo: String, tagNo: String): VProdTag {
        val transferOrder = transferOrderPlusService.findByNo(orderNo)
            ?: throw WebException("调拨指令单【${orderNo}】不存在")

        val tag = prodTagPlusService.findVoByTagNo(tagNo)
            ?: throw WebException("条码【${tagNo}】不存在")

        val details = transferOrderDetailPlusService.listById(transferOrder.id)
        val matched = details.any { d -> d.inventoryCode != null && d.inventoryCode == tag.inventoryCode }

        if (!matched) {
            throw WebException("【${tagNo}】存货编码【${tag.inventoryCode}】不在调拨指令清单中")
        }

        return tag
    }

    /**
     * 检查条码对应的存货编码是否在备料指令单明细中
     *
     * @param prepNo 备料指令单号
     * @param tagNo  纸箱条码
     * @return 标签视图对象
     */
    fun checkPrepTag(prepNo: String, tagNo: String): VProdTag {
        val prepOrder = prepOrderPlusService.findByNo(prepNo)

        val tag = prodTagPlusService.findVoByTagNo(tagNo)
            ?: throw WebException("条码【${tagNo}】不存在")

        val details = prepOrderDetailPlusService.listByPId(prepOrder.id)
        val matched = details.any { d -> d.inventoryCode != null && d.inventoryCode == tag.inventoryCode }

        if (!matched) {
            throw WebException("【${tagNo}】存货编码【${tag.inventoryCode}】不在备料指令清单中")
        }

        return tag
    }

    /**
     * 补打标签，生成并返回标签 PDF。
     * @param tagId 标签主键 ID
     * @return PDF 字节数组
     */
    fun reprint(tagId: String): ByteArray? {
        val vProdTag = prodTagPlusService.findById(tagId)
            ?: throw WebException("标签不存在")
        if (vProdTag.deleted != null && vProdTag.deleted != 0) {
            throw WebException("标签已作废，无法补打")
        }

        val prodOrder = productionOrderPlusService.findById(vProdTag.prodOrderId)
            ?: throw WebException("关联的生产单不存在")
        if (prodOrder.tempId == null) {
            throw WebException("该生产单未绑定打印模板")
        }

        val prodTag = prodTagPlusService.getById(tagId)
        val userInfo = UserContext.require()

        // 记录补打日志（type=1 纸箱标签）
        val printLog = PrintLog().apply {
            id = IdUtil.generateId()
            no = vProdTag.tagNo
            type = 1
            tempId = prodOrder.tempId
            userId = userInfo.userId
            username = userInfo.username
            realName = userInfo.realName
        }
        printLogMapper.insert(printLog)

        log.info("补打纸箱标签: tagNo={}, prodNo={}, userId={}", vProdTag.tagNo, prodOrder.prodNo, userInfo.userId)

        return try {
            generateExcel(prodTag, prodOrder)
        } catch (e: Exception) {
            throw WebException(e.message)
        }
    }

    fun softDelById(id: String) {
        val tag = prodTagPlusService.findById(id)

        // TAG-7：删除前检查是否已打托或已入库，被引用时拒绝删除
        if (palletTagPlusService.findByTagNo(tag.tagNo) != null) {
            throw WebException("【${tag.tagNo}】已打托，不能删除")
        }
        if (stockInTagPlusService.findByTagNo(tag.tagNo) != null) {
            throw WebException("【${tag.tagNo}】已入库，不能删除")
        }

        super.changeDel(
            prodTagPlusService.baseMapper,
            ProdTag::getDeleted,
            1
        ) {
            eq(ProdTag::getId, id)
        }
        log.info("删除/作废纸箱标签: id={}, tagNo={}", id, tag.tagNo)
    }
}
