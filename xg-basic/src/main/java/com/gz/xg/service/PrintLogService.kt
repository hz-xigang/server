package com.gz.xg.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.gz.xg.base.BaseService
import com.gz.xg.domain.entity.PrintLog
import com.gz.xg.domain.search.PrintLogSearch
import com.gz.xg.service.plus.PrintLogPlusService
import org.springframework.stereotype.Service

@Service
class PrintLogService(
    private val plusService: PrintLogPlusService
) : BaseService() {

    fun page(current: Long, size: Long, search: PrintLogSearch): Map<String, Any> {
        val page = Page<PrintLog>(current, size)
        val pageObj = plusService.pageBySearch(page, search)
        return getPage(pageObj)
    }

    /**
     * 分页查询纸箱标签打印日志（type=1）。
     */
    fun pageCartonLabel(current: Long, size: Long, search: PrintLogSearch): Map<String, Any> {
        val cartonSearch = search.copy(type = 1)
        return page(current, size, cartonSearch)
    }

    /**
     * 分页查询打托标签打印日志（type=2）。
     */
    fun pagePalletLabel(current: Long, size: Long, search: PrintLogSearch): Map<String, Any> {
        val palletSearch = search.copy(type = 2)
        return page(current, size, palletSearch)
    }
}
