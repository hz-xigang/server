package com.gz.xg.domain.search

data class ShipOrderSearch (
    val shipNo : String?,
    val customerNo : String?,
    val isTax : Boolean?,
) : BaseSearch()