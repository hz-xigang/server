package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page

abstract class BaseService {
    /**
     * 将分页对象转换为统一返回结构。
     *
     * @param page 分页对象。
     * @return 包含 `total` 与 `records` 的结果映射。
     */
    protected fun <T> getPage(page: Page<T>): Map<String, Any> {
        return mutableMapOf(
            "total" to page.total,
            "records" to page.records
        )
    }

    /**
     * 返回空分页结构。
     *
     * @return 包含空 `records` 与 `total=0` 的结果映射。
     */
    protected fun emptyPage(): Map<String, Any> {
        return mutableMapOf(
            "records" to emptyList<Any>(),
            "total" to 0,
        )
    }
}