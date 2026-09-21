package com.gz.xg.domain.dto

import java.math.BigDecimal

data class UnpackTagVo(
    val tagNo: String,
    val customerCode: String?,
    val erpOrderNo: String?,
    val prodNo: String?,
    val productCategory: String?,
    val spec: String?,
    val inventoryCode: String?,
    val inventoryName: String?,
    val qty: Int?,
    val netWeight: BigDecimal?,
    val grossWeight: BigDecimal?
)
