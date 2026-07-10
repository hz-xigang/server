package com.gz.xg.domain.dto

import java.math.BigDecimal

data class ProdTagTotal(
    val qty: Int,
    val grossWeight: BigDecimal,
    val netWeight: BigDecimal
)