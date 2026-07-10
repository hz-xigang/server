package com.gz.xg.service.plus

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.github.yulichang.base.MPJBaseServiceImpl
import com.gz.xg.domain.entity.TagEntity
import com.gz.xg.exception.WebException
import com.gz.xg.mapper.TagBaseMapper

/**
 * 标签关联表基础服务，封装按标签号和主表主键查询的通用逻辑。
 */
abstract class AbstractTagPlusService<M : TagBaseMapper<T>, T : TagEntity>
    : MPJBaseServiceImpl<M, T>() {

    /**
     * 根据标签号查询一条关联记录。
     */
    fun findByTagNo(tagNo: String): T? {
        val wrapper = QueryWrapper<T>().eq("tagNo", tagNo)
        return getOne(wrapper)
    }

    /**
     * 根据标签号集合查询关联记录。
     */
    fun listByTagNos(tagNos: List<String>): List<T> {
        val wrapper = QueryWrapper<T>().`in`("tagNo", tagNos)
        return list(wrapper)
    }

    /**
     * 根据主表主键查询关联记录。
     */
    fun listByPId(id: String): List<T> {
        val wrapper = QueryWrapper<T>().`in`("pId", id)
        return list(wrapper)
    }

    /**
     * 根据主表主键集合批量查询关联记录。
     */
    fun listByPIds(ids: List<String>): List<T> {
        if (ids.isEmpty()) return emptyList()
        val wrapper = QueryWrapper<T>().`in`("pId", ids)
        return list(wrapper)
    }

    /**
     * 断言指定标签当前不存在关联记录。
     */
    fun assertNotExists(tagNo: String, message: String) {
        if (findByTagNo(tagNo) != null) throw WebException(message)
    }
}