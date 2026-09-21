package com.gz.xg.domain.req

import jakarta.validation.constraints.NotBlank

class UnpackTagReq {

    @NotBlank(message = "请扫描纸箱标签")
    var tagNo: String = ""

    var qtys: List<Int> = listOf()
}
