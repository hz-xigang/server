package com.gz.xg.controller

import com.gz.xg.base.BaseController
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.PalletService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/pallet")
class PalletController(
    private val service: PalletService
) : BaseController()
{


    @PostMapping("")
    fun add (@RequestBody tagNos : List<String>) : ResponseResult{
        service.add(tagNos)
        return success()
    }

    

}