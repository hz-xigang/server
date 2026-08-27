package com.gz.xg.base

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.baomidou.mybatisplus.core.toolkit.support.SFunction
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Service 层基础类，封装分页返回和逻辑删除更新等通用能力。
 */
abstract class BaseService {
    /**
     * 子类通用的日志记录器，按具体子类 Class 进行实例化。
     */
    @JvmField
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 将分页对象转换为统一的返回结构。
     */
    protected fun <T> getPage(page: Page<T>): Map<String, Any> {
        return mutableMapOf(
            "total" to page.total,
            "records" to page.records
        )
    }

    /**
     * 将实体分页结果转换为 DTO 分页结果。
     */
    protected fun <E, D> getDtoPage(page: Page<E>, converter: (List<E>) -> List<D>): Map<String, Any> {
        val dtos = converter(page.records)

        return mapOf(
            "records" to dtos,
            "total" to page.total,
        )
    }

    /**
     * 返回空分页结构，便于上层统一处理。
     */
    protected fun emptyPage(): Map<String, Any> {
        return mutableMapOf(
            "records" to emptyList<Any>(),
            "total" to 0,
        )
    }

    /**
     * 按条件更新逻辑删除标记字段。
     */
    protected fun <E> changeDel(
        baseMapper: BaseMapper<E>,
        deletedColumn: SFunction<E, Int>,
        deleted: Int?,
        builder: LambdaUpdateChainWrapper<E>.() -> Unit,
    ) {
        val success = LambdaUpdateChainWrapper(baseMapper)
            .set(deletedColumn, deleted)
            .apply(builder)
            .update()
        if (!success) {
            log.warn("执行通用逻辑删除/状态变更未匹配到生效记录: deleted={}", deleted)
        }
    }
}