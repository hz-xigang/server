package com.gz.xg.annotation

import com.gz.xg.enums.BusinessType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class OpLog(
    /**
     * 模块或业务标题，例如：文件模板、库位档案、用户管理
     */
    val title: String = "",

    /**
     * 具体操作名称，例如：新增库位、修改密码、用户登录、批量删除
     */
    val opName: String = "",

    /**
     * 业务操作类型
     */
    val businessType: BusinessType = BusinessType.OTHER,

    /**
     * 是否保存请求参数
     */
    val saveParam: Boolean = true,

    /**
     * 是否保存响应结果
     */
    val saveResult: Boolean = false
)
