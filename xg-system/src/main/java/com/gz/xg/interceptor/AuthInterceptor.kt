package com.gz.xg.interceptor

import com.gz.xg.UserContext
import com.gz.xg.exception.WebException
import com.gz.xg.service.JwtService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val jwtService: JwtService
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (HttpMethod.OPTIONS.matches(request.method)) {
            return true
        }

        val authHeader = request.getHeader("Authorization")
        System.err.println(authHeader)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw WebException("未登录或token无效")
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw WebException("未登录或token无效")
        }

        UserContext.set(jwtService.parseToken(token))
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        UserContext.clear()
    }
}