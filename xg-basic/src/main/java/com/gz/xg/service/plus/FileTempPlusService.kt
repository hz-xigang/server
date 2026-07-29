package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.FileTemp
import com.gz.xg.domain.search.FileTempSearch
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.FileTempMapper
import org.springframework.stereotype.Service

@Service
class FileTempPlusService : ServiceImpl<FileTempMapper, FileTemp>() {

    fun existsByNameAndType(name: String, type: Int): FileTemp? {
        return getOne(
            LambdaQueryWrapper<FileTemp>()
                .eq(FileTemp::getName, name)
                .eq(FileTemp::getType, type)
                .eq(FileTemp::getDeleted, 0)
        )
    }

    fun byId(id: String): FileTemp {
        return getById(id) ?: throw WebException("模板不存在")
    }

    /**
     * 分页查询文件模板。
     */
    fun pageBySearch(page: Page<FileTemp>, search: FileTempSearch): Page<FileTemp> {
        val wrapper = MPJLambdaWrapper<FileTemp>()
            .like(!search.name.isNullOrBlank(), FileTemp::getName, search.name)
            .eq(search.type != null, FileTemp::getType, search.type)
            .eq(FileTemp::getDeleted, 0)
            .orderByDesc(FileTemp::getId)
        return page(page, wrapper)
    }

    /**
     * 查询可选模板列表（用于下拉框）。
     */
    fun listForSelect(): List<FileTemp> {
        val wrapper = MPJLambdaWrapper<FileTemp>()
            .select(FileTemp::getId, FileTemp::getName, FileTemp::getType)
            .eq(FileTemp::getDeleted, 0)
            .orderByDesc(FileTemp::getId)
        return list(wrapper)
    }
}
