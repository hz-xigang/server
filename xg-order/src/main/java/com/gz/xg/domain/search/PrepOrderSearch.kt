package com.gz.xg.domain.search

data class PrepOrderSearch(
    val prepNo: String?,
    val customerCode: String?,
    val status: Int?,
) : BaseSearch()
