package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.LocArchiveMapper
import org.springframework.stereotype.Service

/**
 * 库位档案底层服务，封装库位唯一性和存在性校验。
 */
@Service
class LocArchivePlusService : ServiceImpl<LocArchiveMapper, LocArchive>(){

    /**
     * 根据库位编码查询库位。
     */
    fun exitsByCode(code: String) : LocArchive? {
        val wrapper = LambdaQueryWrapper<LocArchive>()
            .eq(LocArchive::getLocCode,code)
        return this.getOne(wrapper)
    }

    /**
     * 根据主键查询库位，不存在时抛出业务异常。
     */
    fun byId(id: String) : LocArchive {
        return getById(id) ?: throw WebException("该库位不存在")
    }

    fun listByCode(codes: List<String>) : List<LocArchive> {
        val wrapper = LambdaQueryWrapper<LocArchive>()
            .`in`(LocArchive::getLocCode,codes)

        return this.list(wrapper)
    }

    fun byCode(code: String) : LocArchive {
        val wrapper = LambdaQueryWrapper<LocArchive>()
            .`in`(LocArchive::getLocCode,code)

        return this.getOne(wrapper)
    }

 }