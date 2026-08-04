package com.gz.xg.u8.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gz.xg.u8.config.U8Config;
import com.gz.xg.u8.dto.U8PoMainQueryRequest;
import com.gz.xg.u8.dto.U8PurchaseOrderMain;
import com.gz.xg.u8.dto.U8Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;

/**
 * 用友 U8 采购订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class U8PurchaseOrderService {

    private final U8Config u8Config;
    private final RestTemplate restTemplate;
    private final Gson gson;

    /**
     * 查询采购订单主表数据
     *
     * @param caccId 账套编号，为 null 则使用配置的默认值
     * @return 采购订单列表
     */
    public U8Response<U8PurchaseOrderMain> queryPurchaseOrderMain(String caccId) {
        String url = u8Config.getUrl() + "/UAP_Po_main_query";

        U8PoMainQueryRequest request = new U8PoMainQueryRequest();
        request.setCacc_id(caccId != null ? caccId : u8Config.getCaccId());

        log.info("调用用友接口查询采购订单，URL: {}, 请求参数: {}", url, gson.toJson(request));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(gson.toJson(request), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            log.info("用友接口响应状态: {}, 响应内容: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Type type = new TypeToken<U8Response<U8PurchaseOrderMain>>(){}.getType();
                U8Response<U8PurchaseOrderMain> result = gson.fromJson(response.getBody(), type);

                if (!result.isSuccess()) {
                    log.error("用友接口返回失败: {}", result.getReturnMessage());
                }

                return result;
            } else {
                log.error("用友接口调用失败，HTTP 状态码: {}", response.getStatusCode());
                return createErrorResponse("HTTP 请求失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("调用用友接口异常", e);
            return createErrorResponse("接口调用异常: " + e.getMessage());
        }
    }

    /**
     * 创建错误响应
     */
    private U8Response<U8PurchaseOrderMain> createErrorResponse(String message) {
        U8Response<U8PurchaseOrderMain> response = new U8Response<>();
        response.setCode("0");
        response.setReturnMessage(message);
        return response;
    }
}
