package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8MomStockInPushRequest
import com.gz.xg.u8.dto.U8MomStockInRequest
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友产成品入库服务
 */
@Service
open class U8MomStockInService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 推送产成品入库单至用友 U8 系统 (/UAP_Rdrecord10_input)
     *
     * @param requestList 入库单主表数据列表
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
    open fun pushMomStockIn(
        requestList: List<U8MomStockInRequest>,
        caccId: String? = null
    ): U8Response<Any> {
        val pushRequest = U8MomStockInPushRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
            this.data = requestList
        }
        return callU8Api("UAP_Rdrecord10_input", pushRequest, object : TypeToken<U8Response<Any>>() {})
    }

    /**
     * 单张产成品入库单快捷推送
     *
     * @param request 入库单主表数据
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
     fun pushMomStockIn(
        request: U8MomStockInRequest,
        caccId: String? = null
    ): U8Response<Any> {
        return pushMomStockIn(listOf(request), caccId)
    }
}
