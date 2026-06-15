package com.gz.xg.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.query.MPJLambdaQueryWrapper
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.domain.entity.ProdTag
import com.gz.xg.domain.mapstruct.ProdTagMapStruct
import com.gz.xg.domain.vo.ProdTagVo
import com.gz.xg.mapper.VProTagMapper
import com.gz.xg.service.plus.ProdTagPlusService
import com.gz.xg.service.plus.ProductionOrderPlusService
import org.springframework.stereotype.Service

@Service
open class ProdTagService(
    private val prodTagPlusService: ProdTagPlusService,
    private val productionOrderPlusService: ProductionOrderPlusService,
    private val sysSequenceService: SysSequenceService,
    private val prodTagMapStruct: ProdTagMapStruct,
    private val vProTagMapper: VProTagMapper
) : BaseService() {

    fun add(dto: ProdTagDto) {
        productionOrderPlusService.findById(dto.prodOrderId)
        val prodTag = prodTagMapStruct.toEntity(dto)
        prodTag.id = IdWorker.getIdStr()
        prodTag.printUser = "001"

        val tagNo = sysSequenceService.generateCarton()
        prodTag.tagNo = tagNo
        prodTagPlusService.save(prodTag)
    }

    fun list(prodNo : String?) : List<ProdTagVo> {
        val wrapper = MPJLambdaWrapper<ProdTagVo>()
            .eq(!prodNo.isNullOrBlank(),ProdTagVo::getProdNo,prodNo)

        return vProTagMapper.selectList(wrapper)
    }






}
