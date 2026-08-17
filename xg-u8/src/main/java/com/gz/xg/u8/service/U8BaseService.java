package com.gz.xg.u8.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gz.xg.exception.WebException;
import com.gz.xg.u8.config.U8Config;
import com.gz.xg.u8.dto.U8Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;

/**
 * 用友 U8 服务基类
 */
@Slf4j
@RequiredArgsConstructor
public abstract class U8BaseService {

    protected final U8Config u8Config;
    protected final RestTemplate u8RestTemplate;
    protected final Gson gson;

    /**
     * 调用用友接口的通用方法
     *
     * @param apiPath 接口路径（如 /UAP_Po_main_query）
     * @param request 请求对象
     * @param typeToken 响应类型 TypeToken
     * @param <T> 响应数据类型
     * @return 用友接口响应
     */
    protected <T> U8Response<T> callU8Api(String apiPath, Object request, TypeToken<U8Response<T>> typeToken) {
        String url = u8Config.getUrl() + apiPath;

        log.info("调用用友接口，URL: {}, 请求参数: {}", url, gson.toJson(request));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(gson.toJson(request), headers);
            ResponseEntity<String> response = u8RestTemplate.postForEntity(url, entity, String.class);

            log.info("用友接口响应状态: {}, 响应内容: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Type type = typeToken.getType();
                U8Response<T> result = gson.fromJson(response.getBody(), type);

                if (!result.isSuccess()) {
                    log.error("用友接口返回失败: {}", result.getReturnMessage());
                    throw new WebException("用友接口返回失败: " + result.getReturnMessage());
                }

                return result;
            } else {
                log.error("用友接口调用失败，HTTP 状态码: {}", response.getStatusCode());
                throw new WebException("用友接口调用失败，HTTP 状态码: " + response.getStatusCode());
            }
        } catch (WebException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用用友接口异常", e);
            throw new WebException("调用用友接口异常: " + e.getMessage(), e);
        }
    }
}
