package com.gz.xg.service

import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.domain.vo.StockTagVo
import com.gz.xg.mapper.VStockTagMapper
import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

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
) : BaseService() {

    /**
     * 新增纸箱标签，并生成标签号。
     */
    fun add(dto: ProdTagDto) {
        productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        prodTag.id = IdUtil.generateId()
        prodTag.printUser = "001"

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)
    }

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listByProdNo(prodNo : String?) : List<ProdTagVo> {
       return prodTagPlusService.listVo(prodNo)
    }

    /**
     * 按标签号查询标签详情，并根据标志位执行占用校验。
     */
    fun findVoByTagNo(tagNo: String, flag: Int): ProdTagVo {
        when (flag) {
            1 -> palletTagPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已打包")
            2 -> stockInTagPlusService.assertNotExists(tagNo, "【${tagNo}】纸箱标签已入库")
        }

        return when(flag) {
            3-> stockInTagPlusService.findVoByTagNo(tagNo)
            else -> prodTagPlusService.findVoByTagNo(tagNo)
        }
    }
}
