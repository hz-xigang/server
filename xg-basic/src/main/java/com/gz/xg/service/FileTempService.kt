package com.gz.xg.service

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
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
import org.slf4j.LoggerFactory
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
            log.warn("新增模板失败: 模板名称已存在 name={}, type={}", dto.name, dto.type)
            throw WebException("【${dto.name}】该模板名称已存在")
        }
        val (userId, username, realName) = UserContext.require()

        val entity = fileTempMapStruct.toEntity(dto)
        entity.id = IdUtil.generateId()
        entity.userId = userId
        entity.realName = realName
        plusService.save(entity)
        log.info("新增模板成功: id={}, name={}, type={}, userId={}, realName={}", entity.id, entity.name, entity.type, userId, realName)
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
        log.info("更新模板成功: id={}, name={}, type={}, width={}, height={}", dto.id, dto.name, dto.type, dto.width, dto.height)
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
        log.info("删除模板成功: id={}", id)
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
        log.info("批量删除模板成功: ids={}", ids)
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

        if (file.isEmpty) {
            log.warn("上传模板文件失败: 上传文件为空 id={}", id)
            throw WebException("上传文件不能为空")
        }

        val originalFilename = file.originalFilename ?: run {
            log.warn("上传模板文件失败: 原始文件名为空 id={}", id)
            throw WebException("文件名不能为空")
        }
        val extension = originalFilename.substringAfterLast('.', "")
        val fileName = if (extension.isNotEmpty()) "prodTag$id.$extension" else "prodTag$id"

        val dirPath = staticYmlConfig.fullTemplate()
        val dir = File(dirPath)
        if (!dir.exists()) {
            val created = dir.mkdirs()
            log.info("模板文件目录不存在，自动创建: path={}, created={}", dirPath, created)
        }

        val targetFile = File(dir, fileName)
        log.info("开始上传模板文件: id={}, originalFilename={}, size={} bytes, targetPath={}", id, originalFilename, file.size, targetFile.absolutePath)

        try {
            file.transferTo(targetFile)
        } catch (e: Exception) {
            log.error("模板文件保存到磁盘失败: id={}, targetPath={}", id, targetFile.absolutePath, e)
            throw WebException("文件上传保存失败: ${e.message}")
        }

        LambdaUpdateChainWrapper(plusService.baseMapper)
            .set(FileTemp::getPath, fileName)
            .eq(FileTemp::getId, id)
            .update()

        log.info("模板文件上传并更新成功: id={}, savedFileName={}", id, fileName)
    }

}
