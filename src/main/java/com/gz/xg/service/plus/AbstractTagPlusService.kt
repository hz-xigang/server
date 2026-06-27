package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.TagBaseMapper

abstract class AbstractTagPlusService<M : TagBaseMapper<T>, T : TagEntity>
    : MPJBaseServiceImpl<M, T>() {

    fun findByTagNo(tagNo: String): T? {
        val wrapper = QueryWrapper<T>().eq("tagNo", tagNo)
        return getOne(wrapper)
    }

    fun listByTagNos(tagNos: List<String>): List<T> {
        val wrapper = QueryWrapper<T>().`in`("tagNo", tagNos)
        return list(wrapper)
    }

    fun listByPId(id: String): List<T> {
        val wrapper = QueryWrapper<T>().`in`("pId", id)
        return list(wrapper)
    }

    fun listByPIds(ids: List<String>): List<T> {
        if (ids.isEmpty()) return emptyList()
        val wrapper = QueryWrapper<T>().`in`("pId", ids)
        return list(wrapper)
    }

    fun assertNotExists(tagNo: String, message: String) {
        if (findByTagNo(tagNo) != null) throw WebException(message)
    }

}
