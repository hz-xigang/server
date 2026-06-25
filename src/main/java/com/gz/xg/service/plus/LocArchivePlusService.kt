package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.base.MPJBaseServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.LocArchive
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.LocArchiveMapper
import org.springframework.stereotype.Service

@Service
class LocArchivePlusService : MPJBaseServiceImpl<LocArchiveMapper, LocArchive>(){

    fun exitsByCode(code: String) :LocArchive? {
        val wrapper = MPJLambdaWrapper<LocArchive>()
            .eq(LocArchive::getLocCode,code)
            .select("top 1 *")
        return this.getOne(wrapper)
    }

    fun byId(id: String) : LocArchive {
        return getById(id) ?: throw WebException("该库位不存在")
    }

 }
