package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8Response
import com.gz.xg.u8.dto.U8SalesStockOutPushRequest
import com.gz.xg.u8.dto.U8SalesStockOutRequest
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友成品出仓(销售出库)服务
 */
@Service
open class U8SalesStockOutService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 推送成品出库单至用友 U8 系统 (/UAP_Rdrecord32_input)
     *
     * @param requestList 成品出库单主表数据列表
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
    open fun pushSalesStockOut(
        requestList: List<U8SalesStockOutRequest>,
        caccId: String? = null
    ): U8Response<Any> {
        val pushRequest = U8SalesStockOutPushRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
            this.data = requestList
        }
        return callU8Api("UAP_Rdrecord32_input", pushRequest, object : TypeToken<U8Response<Any>>() {})
    }

    /**
     * 单张成品出库单快捷推送
     *
     * @param request 成品出库单主表数据
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
    open fun pushSalesStockOut(
        request: U8SalesStockOutRequest,
        caccId: String? = null
    ): U8Response<Any> {
        return pushSalesStockOut(listOf(request), caccId)
    }
}
