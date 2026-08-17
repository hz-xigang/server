package com.gz.xg.service.plus

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.github.yulichang.wrapper.MPJLambdaWrapper
import com.gz.xg.domain.entity.PrintLog
import com.gz.xg.domain.search.PrintLogSearch
import com.gz.xg.mapper.PrintLogMapper
import org.springframework.stereotype.Service

@Service
class PrintLogPlusService : ServiceImpl<PrintLogMapper, PrintLog>() {

    /**
     * 分页查询打印日志。
     */
    fun pageBySearch(page: Page<PrintLog>, search: PrintLogSearch): Page<PrintLog> {
        val wrapper = MPJLambdaWrapper<PrintLog>()
            .like(!search.no.isNullOrBlank(), PrintLog::getNo, search.no)
            .eq(search.type != null, PrintLog::getType, search.type)
            .like(!search.username.isNullOrBlank(), PrintLog::getUsername, search.username)
            .orderByDesc(PrintLog::getCreateTime)
        return page(page, wrapper)
    }
}
