package com.gz.xg.controller

import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProdTagService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/productionTag")
 class ProdTagController(
    private val prodTagService: ProdTagService
)  : BaseController() {

    @PostMapping(value = [""])
     fun add(@RequestBody @Validated dto : ProdTagDto ) : ResponseResult{
        prodTagService.add(dto)
        return success()
    }

    @PostMapping(value = ["list"])
     fun list(@RequestParam("po") po : String) : ResponseResult {
        return success(prodTagService.listByProdNo(po))
    }

    @GetMapping("/tag/{tagNo}")
     fun findByTagNo(@PathVariable tagNo: String,
                     @RequestParam(value = "pallet", defaultValue = "0") pallet : Int ) : ResponseResult{
        return success( prodTagService.findVoByTagNo(tagNo,pallet) )
    }

}