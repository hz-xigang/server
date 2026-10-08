package com.gz.xg.domain.dto

import java.math.BigDecimal

data class ProdTagTotal(
    val qty: BigDecimal,
    val grossWeight: BigDecimal,
    val netWeight: BigDecimal
)