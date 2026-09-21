package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8OtherStockOutPushRequest
import com.gz.xg.u8.dto.U8OtherStockOutRequest
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
open class U8OtherStockOutService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    open fun pushOtherStockOut(
        requestList: List<U8OtherStockOutRequest>,
        caccId: String? = null
    ): U8Response<Any> {
        val pushRequest = U8OtherStockOutPushRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
            this.data = requestList
        }
        return callU8Api("UAP_Rdrecord09_input", pushRequest, object : TypeToken<U8Response<Any>>() {})
    }

    open fun pushOtherStockOut(
        request: U8OtherStockOutRequest,
        caccId: String? = null
    ): U8Response<Any> {
        return pushOtherStockOut(listOf(request), caccId)
    }
}
