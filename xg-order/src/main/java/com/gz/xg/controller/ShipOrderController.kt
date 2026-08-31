package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.domain.search.ShipOrderSearch
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ShipOrderService
import com.gz.xg.service.ShipOrderSyncService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/ship/order")
class ShipOrderController(
    private val service: ShipOrderService,
    private val shipOrderSyncService: ShipOrderSyncService
) : BaseController()
{

    @PostMapping("page")
    fun page(
        @RequestParam("page") current: Long,
        @RequestParam("size") size: Long,
        @RequestBody search: ShipOrderSearch
    ) : ResponseResult
    {
        return success(service.page(current,size,search))
    }

    @GetMapping("")
    fun list() : ResponseResult {
        return success(service.list())
    }

    /**
     * 手动触发同步 U8 发货单
     */
    @PostMapping("sync")
    fun sync(@RequestParam(value = "caccId", required = false) caccId: String?): ResponseResult {
        val count = shipOrderSyncService.syncShipOrders(caccId ?: "108")
        return success(count)
    }

}