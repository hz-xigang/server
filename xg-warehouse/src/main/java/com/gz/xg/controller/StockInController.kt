package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.req.AddStockIn
import com.gz.xg.domain.search.StockSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockInService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/stock/in")
class StockInController(
    private val service: StockInService
) : BaseController()
{
    @PostMapping("")
    @OpLog(title = "入库管理", opName = "产品/退货入库", businessType = BusinessType.INSERT)
    fun add (@RequestBody req : AddStockIn,
             @RequestParam(defaultValue = "0" )type : Int) : ResponseResult {

        val result = if (type == 1) {
            service.addReturn(req)
        } else {
            service.add(req)
        }

        // 如果存在未完成的条码，返回自定义状态码与未完成条码明细
        if (result.failTags.isNotEmpty()) {
            val res = ResponseResult()
            res.code = 10008
            res.isSuccess = false
            res.message = "部分条码入库失败(共${result.failTags.size}件)，请核对后重试"
            res.data = result.failTags
            return res
        }

        return success("入库成功，共${result.successCount}件")
    }

    @PostMapping("page")
    fun page(
        @RequestBody search: StockSearch,
        @RequestParam(value = "page", defaultValue = "1") current: Long,
        @RequestParam(value = "size", defaultValue = "15") size: Long,
        ) : ResponseResult
    {
        return success(
            service.page(search, current, size)
        )
    }


}