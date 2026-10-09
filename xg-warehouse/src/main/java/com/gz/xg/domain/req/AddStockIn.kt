package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class AddStockIn {

    @NotBlank(message = "请选择库位")
    val locId: String = ""

    /**
     * 仓库编码(如: 01)
     */
    val warehouseCode: String? = null

    val tagNos: List<String> = listOf()

}