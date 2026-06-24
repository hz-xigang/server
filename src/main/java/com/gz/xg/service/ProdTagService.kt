package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.PalletMapper
import com.gz.xg.service.plus.PalletTagPlusService
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

@Service
 class ProdTagService(
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService,
    private val prodTagMapStruct: ProdTagMapStruct,
     private val palletTagPlusService: PalletTagPlusService
) : BaseService() {

    fun add(dto: ProdTagDto) {
        productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        prodTag.id = IdUtil.generateId()
        prodTag.printUser = "001"

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)
    }

    fun listByProdNo(prodNo : String?) : List<ProdTagVo> {
       return prodTagPlusService.listVo(prodNo)
    }

    fun findVoByTagNo(tagNo : String,pallet : Int) : ProdTagVo {
        if (pallet != 0){

            val palletTag = palletTagPlusService.findByTagNo(tagNo)

            if (palletTag != null){
                throw WebException("【${tagNo}】纸箱标签已打包")
            }
        }
        return prodTagPlusService.findVoByTagNo(tagNo)
    }






}
