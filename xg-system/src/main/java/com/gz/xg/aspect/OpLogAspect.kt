package com.gz.xg.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import com.gz.xg.UserContext
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.SysOpLog
import com.gz.xg.filter.TraceFilter
import com.gz.xg.mapper.SysOpLogMapper
import com.gz.xg.util.IdUtil
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@Aspect
@Component
class OpLogAspect(
    private val sysOpLogMapper: SysOpLogMapper,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(OpLogAspect::class.java)

    @Around("@annotation(opLog)")
    fun around(joinPoint: ProceedingJoinPoint, opLog: OpLog): Any? {
        val startTime = System.currentTimeMillis()
        val sysOpLog = SysOpLog().apply {
            id = IdUtil.generateId()
            requestId = MDC.get(TraceFilter.MDC_KEY) ?: IdUtil.generateId()
            title = opLog.title
            opName = if (opLog.opName.isNotBlank()) opLog.opName else getDefaultOpName(opLog)
            businessType = opLog.businessType.value
            method = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"
            operTime = LocalDateTime.now()
        }

        runCatching {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val request: HttpServletRequest? = requestAttributes?.request
            request?.let {
                sysOpLog.requestMethod = it.method
                sysOpLog.operUrl = it.requestURI
                sysOpLog.operIp = getIpAddress(it)
            }
        }

        UserContext.get()?.let { user ->
            sysOpLog.operUserId = user.userId
            sysOpLog.operName = user.username
            sysOpLog.operRealName = user.realName
        }

        if (opLog.saveParam && joinPoint.args.isNotEmpty()) {
            runCatching {
                val filterArgs = joinPoint.args.filter { it !is ServletRequest && it !is ServletResponse }
                sysOpLog.operParam = objectMapper.writeValueAsString(filterArgs)
            }
        }

        var result: Any? = null
        try {
            result = joinPoint.proceed()
            sysOpLog.status = 0

            if (opLog.saveResult && result != null) {
                runCatching {
                    sysOpLog.jsonResult = objectMapper.writeValueAsString(result)
                }
            }
        } catch (e: Exception) {
            sysOpLog.status = 1
            sysOpLog.errorMsg = e.message
            sysOpLog.stackTrace = e.stackTraceToString()
            throw e
        } finally {
            sysOpLog.costTime = System.currentTimeMillis() - startTime
            runCatching {
                sysOpLogMapper.insert(sysOpLog)
            }.onFailure { ex ->
                log.error("Failed to insert SysOpLog: ${ex.message}", ex)
            }
        }

        return result
    }

    private fun getDefaultOpName(opLog: OpLog): String {
        return when (opLog.businessType.value) {
            1 -> "新增"
            2 -> "修改"
            3 -> "删除"
            4 -> "上传"
            5 -> "查询"
            6 -> "同步"
            else -> "操作"
        }
    }

    private fun getIpAddress(request: HttpServletRequest): String {
        val ip = request.getHeader("X-Forwarded-For")
        if (!ip.isNullOrEmpty() && !"unknown".equals(ip, ignoreCase = true)) {
            return ip.split(",")[0].trim()
        }
        return request.remoteAddr ?: ""
    }
}
