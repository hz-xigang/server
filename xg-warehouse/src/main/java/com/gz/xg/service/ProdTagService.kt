package com.gz.xg.service

import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.view.VProdTag

import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.service.plus.StockInTagPlusService
import com.gz.xg.service.plus.StockInventoryPlusService
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
     private val stockInventoryPlusService: StockInventoryPlusService
) : BaseService() {

    /**
     * 新增纸箱标签，并生成标签号。
     *
     */
    fun add(dto: ProdTagDto) {
        productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        val (userId, username) = UserContext.require()
        prodTag.id = IdUtil.generateId()
        prodTag.userId = userId
        prodTag.username = username

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)
    }

    /**
     * 按生产单号查询标签视图列表。
     */
    fun listByProdNo(prodNo : String?) : List<VProdTag> {
       return prodTagPlusService.listVo(prodNo)
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
}
