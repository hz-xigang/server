package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8PurchaseStockInErrorDetail
import com.gz.xg.u8.dto.U8PurchaseStockInPushRequest
import com.gz.xg.u8.dto.U8PurchaseStockInRequest
import com.gz.xg.u8.dto.U8Response
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 用友 U8 采购入库服务
 */
@Service
open class U8PurchaseStockInService(
    u8Config: U8Config,
    u8RestTemplate: RestTemplate,
    gson: Gson
) : U8BaseService(u8Config, u8RestTemplate, gson) {

    /**
     * 推送采购入库单数据至用友 U8 系统
     * 接口路径: UAP_Rdrecord01_input
     *
     * @param requestList 采购入库单列表
     * @param caccId 账套编号，为 null 则使用配置中的默认值
     * @return 用友响应结果（成功时 Data 为空，失败时 Data 包含明细报错信息）
     */
    open fun pushPurchaseStockIn(
        requestList: List<U8PurchaseStockInRequest>,
        caccId: String? = null
    ): U8Response<U8PurchaseStockInErrorDetail> {
        val pushRequest = U8PurchaseStockInPushRequest().apply {
            this.caccId = caccId ?: u8Config.caccId
            this.data = requestList
        }

        return callU8Api(
            "UAP_Rdrecord01_input",
            pushRequest,
            object : TypeToken<U8Response<U8PurchaseStockInErrorDetail>>() {}
        )
    }

    /**
     * 推送单张采购入库单数据至用友 U8 系统
     *
     * @param singleRequest 单张采购入库单
     * @param caccId 账套编号，为 null 则使用配置中的默认值
     * @return 用友响应结果
     */
    open fun pushPurchaseStockIn(
        singleRequest: U8PurchaseStockInRequest,
        caccId: String? = null
    ): U8Response<U8PurchaseStockInErrorDetail> {
        return pushPurchaseStockIn(listOf(singleRequest), caccId)
    }
}
