package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8Response
import com.gz.xg.u8.dto.U8SalesOrderMain
import com.gz.xg.u8.dto.U8SoMainQueryRequest
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友 U8 销售订单服务
 */
@Service
open class U8SalesOrderService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 查询销售订单主表数据
     *
     * @param caccId 账套编号，为 null 则使用配置的默认值
     * @return 销售订单列表
     */
    open fun querySalesOrderMain(caccId: String?): U8Response<U8SalesOrderMain> {
        val request = U8SoMainQueryRequest()
        request.caccId = caccId ?: u8Config.caccId

        return callU8Api("/UAP_SO_main_query", request, object : TypeToken<U8Response<U8SalesOrderMain>>() {})
    }
}
