package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class AddStockOrder {

     @NotBlank(message = "请选择库位")
     val no: String = ""

     val tagNos: List<String> = listOf()
 }
