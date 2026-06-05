package com.gz.xg.controller

import com.gz.xg.domain.dto.ProdTagDto
import com.gz.xg.exception.ResponseResult
import com.gz.xg.service.ProdTagService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/productionTag")
open class ProdTagController(
    private val prodTagService: ProdTagService
)  : BaseController() {

    @PostMapping(value = [""])
    open fun add(@RequestBody @Validated dto : ProdTagDto ) : ResponseResult{
        prodTagService.add(dto)
        return success()
    }

}