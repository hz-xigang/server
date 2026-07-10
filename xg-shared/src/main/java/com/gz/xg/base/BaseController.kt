package com.gz.xg.base

import com.gz.xg.UserContext
import com.gz.xg.domain.auth.LoginUser
import com.gz.xg.exception.ResponseResult
import org.springframework.stereotype.Component

/**
 * 基础Controller类
 * 提供通用的Controller功能，包括：
 * - 统一的成功响应方法
 * - JWT Token解析和用户信息提取
 * - 公司ID获取
 *
 * 改进：
 * - 使用UserContext获取用户信息（由拦截器设置）
 * - 保留getClaims方法用于特殊场景（如刷新token）
 *
 * @author system
 */
@Component
 class BaseController {


    /**
     * 返回成功响应（无数据）
     *
     * @return 成功响应结果
     */
    protected fun success(): ResponseResult {
        return ResponseResult.success()
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 响应数据
     * @return 成功响应结果
     */
    protected fun success(data: Any): ResponseResult {
        return ResponseResult.success(data)
    }

    protected fun currentUser(): LoginUser {
        return UserContext.require()
    }
}