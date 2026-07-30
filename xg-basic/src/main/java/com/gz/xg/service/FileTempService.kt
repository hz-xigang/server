package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.UserContext
import com.gz.xg.base.BaseService
import com.gz.xg.config.resource.StaticYmlConfig
import com.gz.xg.domain.dto.FileTempDto
import com.gz.xg.domain.entity.FileTemp
import com.gz.xg.domain.mapstruct.FileTempMapStruct
import com.gz.xg.domain.search.FileTempSearch
import com.gz.xg.exception.WebException
import com.gz.xg.service.plus.FileTempPlusService
import com.gz.xg.util.IdUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Service
class FileTempService(
    private val plusService: FileTempPlusService,
    private val fileTempMapStruct: FileTempMapStruct,
    private val staticYmlConfig: StaticYmlConfig
) : BaseService() {

    fun add(dto: FileTempDto) {
        if (plusService.existsByNameAndType(dto.name, dto.type) != null) {
            throw WebException("【${dto.name}】该模板名称已存在")
        }
        val (userId, username,realName) = UserContext.require()

        val entity = fileTempMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()
        entity.userId = userId
        entity.realName = realName
        plusService.save(entity)
    }

    fun page(current: Long, size: Long, search: FileTempSearch): Map<String, Any> {
        val page = Page<FileTemp>(current, size)
        val pageObj = plusService.pageBySearch(page, search)
        return getDtoPage(pageObj, fileTempMapStruct::toDtoList)
    }

    /**
     * 根据主键更新文件模板信息。
     */
    fun updateById(dto: FileTempDto) {
        plusService.byId(dto.id)

        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(FileTemp::getName, dto.name)
            .set(FileTemp::getWidth, dto.width)
            .set(FileTemp::getHeight, dto.height)
            .set(FileTemp::getType, dto.type)
            .eq(FileTemp::getId, dto.id)
            .update()
    }

    /**
     * 逻辑删除单个文件模板。
     */
    fun changeDeleteById(id: String) {
        plusService.byId(id)

        changeDel(
            plusService.baseMapper,
            FileTemp::getDeleted,
            1
        ) { eq(FileTemp::getId, id) }
    }

    /**
     * 批量逻辑删除文件模板。
     */
    fun changeDeleteByIds(ids: List<String>) {
        changeDel(
            plusService.baseMapper,
            FileTemp::getDeleted,
            1
        ) { `in`(FileTemp::getId, ids) }
    }

    /**
     * 查询可选文件模板列表，通常用于下拉框场景。
     */
    fun list(): List<FileTempDto> {
        val list = plusService.listForSelect()
        return fileTempMapStruct.toDtoList(list)
    }

    fun uploadFile(id: String, file: MultipartFile) {
        plusService.byId(id)

        val originalFilename = file.originalFilename ?: throw WebException("文件名不能为空")
        val extension = originalFilename.substringAfterLast('.', "")
        val fileName = if (extension.isNotEmpty()) "prodTag$id.$extension" else "prodTag$id"

        val dir = File(staticYmlConfig.fullTemplate())
        if (!dir.exists()) dir.mkdirs()

        file.transferTo(File(dir, fileName))

        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(FileTemp::getPath, fileName)
            .eq(FileTemp::getId, id)
            .update()
    }

}
