package com.gz.xg.domain.search

data class ProdTagSearch(
    val prodNo : String?,
    val customerCode : String?,
    val inventoryName : String?,
): BaseSearch()