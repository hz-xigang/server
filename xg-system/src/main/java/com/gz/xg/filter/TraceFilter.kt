package com.gz.xg.filter

import com.gz.xg.util.IdUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 请求全链路追踪过滤器：
 * 每一个 HTTP 请求进入系统时，自动生成全局唯一的流水号 requestId，
 * 并将其绑定到 SLF4J MDC 线程上下文中供业务 log 自动打印，同时回传在响应头中。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TraceFilter : OncePerRequestFilter() {

    companion object {
        const val TRACE_ID_HEADER = "X-Request-Id"
        const val MDC_KEY = "requestId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = IdUtil.generateId()

        // 注入 SLF4J MDC 线程上下文
        MDC.put(MDC_KEY, requestId)
        // 回传给响应头，便于排查与调用方对齐
        response.setHeader(TRACE_ID_HEADER, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            // 请求完成必须清理，防止线程池复用导致上下文错乱
            MDC.remove(MDC_KEY)
        }
    }
}
