package com.gz.xg.u8.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gz.xg.exception.WebException
import com.gz.xg.u8.config.U8Config
import com.gz.xg.u8.dto.U8Response
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

/**
 * 用友 U8 服务基类
 */
abstract class U8BaseService(
    protected val u8Config: U8Config,
    protected val u8RestTemplate: RestTemplate,
    protected val gson: Gson
) {
    private val log = LoggerFactory.getLogger(U8BaseService::class.java)

    /**
     * 调用用友接口的通用方法
     *
     * @param apiPath 接口路径（如 /UAP_Po_main_query）
     * @param request 请求对象
     * @param typeToken 响应类型 TypeToken
     * @param <T> 响应数据类型
     * @return 用友接口响应
     */
    protected fun <T> callU8Api(
        apiPath: String,
        request: Any,
        typeToken: TypeToken<U8Response<T>>
    ): U8Response<T> {
        // 确保不会出现重复的斜杠
        val url = if (u8Config.url.endsWith("/") && apiPath.startsWith("/")) {
            u8Config.url + apiPath.substring(1)
        } else if (!u8Config.url.endsWith("/") && !apiPath.startsWith("/")) {
            u8Config.url + "/" + apiPath
        } else {
            u8Config.url + apiPath
        }

        log.info("调用用友接口，URL: {}, 请求参数: {}", url, gson.toJson(request))

        try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON

            val entity = HttpEntity(gson.toJson(request), headers)
            val response = u8RestTemplate.postForEntity(url, entity, String::class.java)

            log.info("用友接口响应状态: {}, 响应内容: {}", response.statusCode, response.body)

            if (response.statusCode == HttpStatus.OK && response.body != null) {
                val type = typeToken.type
                val result = gson.fromJson<U8Response<T>>(response.body, type)

                if (!result.isSuccess) {
                    log.error("用友接口返回失败: {}", result.returnMessage)
                    throw WebException("用友接口返回失败: " + result.returnMessage)
                }

                return result
            } else {
                log.error("用友接口调用失败，HTTP 状态码: {}", response.statusCode)
                throw WebException("用友接口调用失败，HTTP 状态码: " + response.statusCode)
            }
        } catch (e: WebException) {
            throw e
        } catch (e: Exception) {
            log.error("调用用友接口异常", e)
            throw WebException("调用用友接口异常: " + e.message, e)
        }
    }
}
