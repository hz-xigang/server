package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8PoMainQueryRequest
import com.gz.xg.u8.dto.U8PurchaseOrderMain
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友 U8 采购订单服务
 */
@Service
open class U8PurchaseOrderService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 查询采购订单主表数据
     *
     * @param caccId 账套编号，为 null 则使用配置的默认值
     * @return 采购订单列表
     */
    open fun queryPurchaseOrderMain(caccId: String?): U8Response<U8PurchaseOrderMain> {
        val request = U8PoMainQueryRequest()
        request.cacc_id = caccId ?: u8Config.caccId

        return callU8Api("UAP_Pomain_query", request, object : TypeToken<U8Response<U8PurchaseOrderMain>>() {})
    }
}
