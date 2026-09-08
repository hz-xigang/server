package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class AddStockOrder {

     @NotBlank(message = "指令单号不能为空")
     val no: String = ""

     val tagNos: List<String> = listOf()

    val locCode : String = ""
 }
