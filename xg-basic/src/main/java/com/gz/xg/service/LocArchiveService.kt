package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.base.BaseService
import com.gz.xg.domain.dto.LocArchiveDto
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.domain.mapstruct.LocArchiveMapStruct
import com.gz.xg.domain.search.LocArchiveSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.LocArchivePlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service

/**
 * 库位档案服务，负责库位信息的新增、分页查询、更新和逻辑删除。
 */
@Service
class LocArchiveService(
    private val plusService: LocArchivePlusService,
    private val locArchiveMapStruct: LocArchiveMapStruct
) : BaseService() {

    /**
     * 新增库位档案，并校验库位编码唯一性。
     */
    fun add(dto: LocArchiveDto) {
        val archive = plusService.exitsByCode(dto.locCode)
        if (archive != null) {
            throw WebException("【${dto.locCode}】该编码已存在")
        }
        val entity = locArchiveMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()

        plusService.save(entity)
    }

    /**
     * 分页查询库位档案，支持按编码和类型筛选。
     */
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

    /**
     * 根据主键更新库位信息。
     */
    fun updateById(dto: LocArchiveDto) {
        plusService.byId(dto.id)
        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(LocArchive::getStatus, dto.status)
            .set(LocArchive::getLocCode, dto.locCode)
            .set(LocArchive::getLocType, dto.locType)
            .eq(LocArchive::getId, dto.id)
            .update()
    }

    /**
     * 逻辑删除单个库位。
     */
    fun changeDeleteById(id: String) {
        plusService.byId(id)
        changeDel<LocArchive>(
            plusService.baseMapper,
            LocArchive::getDeleted,
            1
        ) { eq(LocArchive::getId, id) }
    }

    /**
     * 批量逻辑删除库位。
     */
    fun changeDeleteByIds(ids: List<String>) {
        changeDel(
            plusService.baseMapper,
            LocArchive::getDeleted,
            1
        ) { `in`(LocArchive::getId, ids) }
    }

    /**
     * 查询可选库位列表，通常用于下拉框场景。
     */
    fun list() : List<LocArchiveDto>{
        val wrapper = MPJLambdaWrapper<LocArchive>()
            .select(LocArchive::getId, LocArchive::getLocCode)
            .eq(LocArchive::getDeleted, 0)
            .orderByDesc(LocArchive::getId)
        val list = plusService.list(wrapper)
        return locArchiveMapStruct.toDtoList(list)
    }
}
