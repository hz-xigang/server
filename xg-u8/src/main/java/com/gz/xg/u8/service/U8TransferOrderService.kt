package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8Response
import com.gz.xg.u8.dto.U8TransVouchQueryRequest
import com.gz.xg.u8.dto.U8TransferOrderMain
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友 U8 调拨单服务
 */
@Service
 class U8TransferOrderService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 查询调拨单主表及明细数据
     *
     * @param caccId 账套编号，为 null 则使用配置的默认值
     * @return 调拨单列表
     */
     fun queryTransferOrderMain(caccId: String?): U8Response<U8TransferOrderMain> {
        val request = U8TransVouchQueryRequest()
        request.cacc_id = caccId ?: u8Config.caccId

        return callU8Api("UAP_TransVouch_query", request, object : TypeToken<U8Response<U8TransferOrderMain>>() {})
    }
}
