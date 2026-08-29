package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8Response
import com.gz.xg.u8.dto.U8StockMovePushRequest
import com.gz.xg.u8.dto.U8StockMoveRequest
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友移库作业服务
 */
@Service
open class U8StockMoveService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 推送移库作业单至用友 U8 系统 (/UAP_AdjustPVouch_input)
     *
     * @param requestList 移库单主表数据列表
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
    open fun pushStockMove(
        requestList: List<U8StockMoveRequest>,
        caccId: String? = null
    ): U8Response<Any> {
        val pushRequest = U8StockMovePushRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
            this.data = requestList
        }
        return callU8Api("UAP_AdjustPVouch_input", pushRequest, object : TypeToken<U8Response<Any>>() {})
    }

    /**
     * 单张移库作业单快捷推送
     *
     * @param request 移库单主表数据
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 用友接口统一响应
     */
    open fun pushStockMove(
        request: U8StockMoveRequest,
        caccId: String? = null
    ): U8Response<Any> {
        return pushStockMove(listOf(request), caccId)
    }
}
