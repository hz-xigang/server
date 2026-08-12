package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PalletService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/pallet")
class PalletController(
    private val service: PalletService
) : BaseController()
{

    @PostMapping("")
    fun add (@RequestBody tagNos : List<String>) : ResponseResult{
        return success(service.add(tagNos))
    }

    @GetMapping("/tags/{palletNo}")
    fun findTags(@PathVariable palletNo: String,
                 @RequestParam(value = "type", defaultValue = "0") type : Int ) : ResponseResult
    {
        return success(service.findTagsByPalletNo(palletNo, type))
    }

    @PostMapping("/unbundle/{palletNo}")
    fun unbundle(@PathVariable palletNo: String,
                 @RequestBody tagNos: List<String>) : ResponseResult
    {
        service.unbundle(palletNo, tagNos)
        return success()
    }

}