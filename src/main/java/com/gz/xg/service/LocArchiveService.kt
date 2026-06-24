package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.dto.LocArchiveDto
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.mapstruct.LocArchiveMapStruct
import com.gz.xg.domain.search.LocArchiveSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

@Service
class LocArchiveService(
    private val plusService: LocArchivePlusService,
    private val locArchiveMapStruct: LocArchiveMapStruct
) : BaseService() {

    fun add(dto: LocArchiveDto) {

        // 1- 判断编号是否存在
        val archive = plusService.exitsByCode(dto.locCode)
        if (archive != null) {
            throw WebException("该编码已存在")
        }
        val entity = locArchiveMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()

        plusService.save(entity)
    }

    fun page(current : Long, size: Long,search: LocArchiveSearch) : Map<String, Any> {
        val page = Page<LocArchive>(current, size)

        val wrapper = MPJLambdaWrapper<LocArchive>()
            .eq(!search.code.isNullOrBlank(),LocArchive::getLocCode,search.code)
            .eq(!search.type.isNullOrBlank(), LocArchive::getLocType,search.type)
            .orderByDesc(LocArchive::getId)
        val pageObj = plusService.page(page, wrapper)
        return getDtoPage(pageObj, locArchiveMapStruct::toDtoList)
    }

    fun findById(id: String): LocArchive? {
        return plusService.getById(id)
    }

    fun deleteById(id: String): Boolean {
        return plusService.removeById(id)
    }

    fun updateById(entity: LocArchive): Boolean {
        return plusService.updateById(entity)
    }
}
