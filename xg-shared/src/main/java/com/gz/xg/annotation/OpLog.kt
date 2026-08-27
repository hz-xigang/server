package com.gz.xg.annotation

import com.gz.xg.enums.BusinessType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OpLog(
    val title: String = "",
    val businessType: BusinessType = BusinessType.OTHER,
    val saveParam: Boolean = true,
    val saveResult: Boolean = false
)
