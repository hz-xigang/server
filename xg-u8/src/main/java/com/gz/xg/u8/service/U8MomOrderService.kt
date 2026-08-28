package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8MomOrderMain
import com.gz.xg.u8.dto.U8MomOrderQueryRequest
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友生产订单服务
 */
@Service
open class U8MomOrderService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 查询生产订单列表
     *
     * @param caccId 账套编号（为空时取配置中的默认账套）
     * @return 生产订单主表响应数据
     */
    open fun queryMomOrderMain(caccId: String? = null): U8Response<U8MomOrderMain> {
        val request = U8MomOrderQueryRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
        }
        return callU8Api("UAP_MOM_order_query", request, object : TypeToken<U8Response<U8MomOrderMain>>() {})
    }
}
