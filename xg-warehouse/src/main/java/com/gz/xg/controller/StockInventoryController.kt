package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.StockInventoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/inventory")
class StockInventoryController(
    private val service: StockInventoryService
)  : BaseController()
{

    @GetMapping("summary")
    fun summary() : ResponseResult{
        return success(service.inventorySummary())
    }

    @GetMapping("tags/{locId}")
    fun findTagByLocId(@PathVariable locId: String) : ResponseResult{
        return success(service.findTagByLocId(locId))
    }

}