package com.gz.xg.service

import com.baomidou.mybatisplus.core.toolkit.IdWorker
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.query.MPJQueryWrapper
import com.github.yulichang.toolkit.JoinWrappers
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
            throw WebException("【${dto.locCode}】该编码已存在")
        }
        val entity = locArchiveMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()

        plusService.save(entity)
    }

    fun page(current: Long, size: Long, search: LocArchiveSearch): Map<String, Any> {
        val page = Page<LocArchive>(current, size)

        val wrapper = MPJLambdaWrapper<LocArchive>()
            .like(!search.code.isNullOrBlank(), LocArchive::getLocCode, search.code)
            .eq(!search.type.isNullOrBlank(), LocArchive::getLocType, search.type)
            .eq(LocArchive::getDeleted,0)
            .orderByDesc(LocArchive::getId)
        val pageObj = plusService.page(page, wrapper)
        return getDtoPage(pageObj, locArchiveMapStruct::toDtoList)
    }

    fun updateById(dto: LocArchiveDto) {
        plusService.byId(dto.id)
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(LocArchive::getStatus, dto.status)
            .set(LocArchive::getLocCode, dto.locCode)
            .set(LocArchive::getLocType, dto.locType)
            .eq(LocArchive::getId, dto.id)
            .update() // 直接执行更新
    }

    fun changeDeleteById(id: String) {
        plusService.byId(id)
        changeDel<LocArchive>(
            plusService.baseMapper,
            LocArchive::getDeleted,
            1
        ) { eq(LocArchive::getId, id) }
    }

    fun changeDeleteByIds(ids: List<String>) {
        changeDel<LocArchive>(
            plusService.baseMapper,
            LocArchive::getDeleted,
            1
        ) { `in`(LocArchive::getId, ids) }
    }

    fun list() : List<LocArchiveDto>{
        val wrapper = MPJLambdaWrapper<LocArchive>()
            .select(LocArchive::getId, LocArchive::getLocCode)
            .eq(LocArchive::getDeleted, 0)
            .orderByDesc(LocArchive::getId)
        val list = plusService.list(wrapper)
        return locArchiveMapStruct.toDtoList(list)
    }

}
