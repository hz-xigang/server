package com.gz.xg.controller

import com.gz.xg.annotation.OpLog
import com.gz.xg.base.BaseController
import com.gz.xg.domain.dto.ProdOrderDto
import com.gz.xg.domain.search.ProdOrderSearch
import com.gz.xg.enums.BusinessType
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProdOrderService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prod")
open class ProdOrderController(
    private val service: ProdOrderService
) : BaseController() {

    @GetMapping(value = ["/pgNo/{pgNo}"])
    fun findByProdNo(@PathVariable pgNo: String) : ResponseResult {
        return success(service.findByProgNo(pgNo))
    }

    @PostMapping(value = ["/page"])
    fun page(
        @RequestParam(value = "page", defaultValue = "1") page: Long,
        @RequestParam(value = "size", defaultValue = "1") size: Long,
        @RequestBody search: ProdOrderSearch
    ) : ResponseResult
    {
        return success(service.page(page,size,search))
    }

    @PutMapping("")
    @OpLog(title = "生产单管理", opName = "修改生产单", businessType = BusinessType.UPDATE)
    fun edit(@RequestBody dto: ProdOrderDto) : ResponseResult {
        service.edit(dto)
        return success()
    }

    @DeleteMapping("{id}")
    @OpLog(title = "生产单管理", opName = "删除生产单", businessType = BusinessType.DELETE)
    fun softDel(@PathVariable id: String) : ResponseResult {
        service.softDel(id)
        return success()
    }

}
