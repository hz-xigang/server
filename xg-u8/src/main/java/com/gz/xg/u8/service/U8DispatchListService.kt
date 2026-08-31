package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8DispatchListMain
import com.gz.xg.u8.dto.U8DispatchListQueryRequest
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友发货单查询服务 (UAP_dispatchlist_query)
 */
@Service
open class U8DispatchListService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 查询用友发货单列表
     *
     * @param caccId 账套编号（为空时取默认配置）
     * @return 用友接口统一响应，包含发货单主表与明细数据
     */
    open fun queryDispatchList(caccId: String? = null): U8Response<U8DispatchListMain> {
        val request = U8DispatchListQueryRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
        }
        return callU8Api("UAP_dispatchlist_query", request, object : TypeToken<U8Response<U8DispatchListMain>>() {})
    }
}
