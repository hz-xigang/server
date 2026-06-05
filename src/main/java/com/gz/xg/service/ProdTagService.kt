package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import org.springframework.stereotype.Service

@Service
open class ProdTagService(
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService,
   private val prodTagMapStruct: ProdTagMapStruct
) {

    fun add(dto: ProdTagDto){
        productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        prodTag.id = IdWorker.getIdStr()
        prodTag.printUser = "001"

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)

    }


}
