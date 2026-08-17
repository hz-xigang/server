package com.gz.xg.u8.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gz.xg.u8.config.U8Config;
import com.gz.xg.u8.dto.U8Response;
import com.gz.xg.u8.dto.U8SalesOrderMain;
import com.gz.xg.u8.dto.U8SoMainQueryRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 用友 U8 销售订单服务
 */
@Service
public class U8SalesOrderService extends U8BaseService {

    public U8SalesOrderService(U8Config u8Config, RestTemplate u8RestTemplate, Gson gson) {
        super(u8Config, u8RestTemplate, gson);
    }

    /**
     * 查询销售订单主表数据
     *
     * @param caccId 账套编号，为 null 则使用配置的默认值
     * @return 销售订单列表
     */
    public U8Response<U8SalesOrderMain> querySalesOrderMain(String caccId) {
        U8SoMainQueryRequest request = new U8SoMainQueryRequest();
        request.setCacc_id(caccId != null ? caccId : u8Config.getCaccId());

        return callU8Api("/UAP_SO_main_query", request, new TypeToken<U8Response<U8SalesOrderMain>>(){});
    }
}
