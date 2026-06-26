package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class AddStockIn {

    @NotBlank(message = "请选择库位")
    val locId: String = ""

    val tagNos: List<String> = listOf()

}
