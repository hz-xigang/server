package com.gz.xg.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import com.gz.xg.UserContext
import com.gz.xg.annotation.OpLog
import com.gz.xg.domain.entity.SysOpLog
import com.gz.xg.enums.BusinessType
import com.gz.xg.mapper.SysOpLogMapper
import com.gz.xg.util.IdUtil
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
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
            title = opLog.title
            opName = opLog.opName.ifBlank { getDefaultOpName(opLog.businessType) }
            businessType = opLog.businessType.value
            method = "${joinPoint.signature.declaringTypeName}.${joinPoint.signature.name}"
            operTime = LocalDateTime.now()
        }

        runCatching {
            val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            val request: HttpServletRequest? = requestAttributes?.request
            if (request != null) {
                sysOpLog.requestMethod = request.method
                sysOpLog.operUrl = request.requestURI
                sysOpLog.operIp = getIpAddress(request)
            } else {
                sysOpLog.requestMethod = "SCHEDULE"
                sysOpLog.operUrl = "INTERNAL"
                sysOpLog.operIp = "127.0.0.1"
            }
        }

        val currentUser = UserContext.get()
        if (currentUser != null) {
            sysOpLog.operUserId = currentUser.userId
            sysOpLog.operName = currentUser.username
            sysOpLog.operRealName = currentUser.realName
        } else {
            sysOpLog.operUserId = "0"
            sysOpLog.operName = "SYSTEM"
            sysOpLog.operRealName = "系统任务"
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

    private fun getDefaultOpName(businessType: BusinessType): String {
        return when (businessType) {
            BusinessType.INSERT -> "新增"
            BusinessType.UPDATE -> "修改"
            BusinessType.DELETE -> "删除"
            BusinessType.IMPORT -> "导入"
            BusinessType.EXPORT -> "导出"
            BusinessType.UPLOAD -> "上传"
            BusinessType.SELECT -> "查询"
            BusinessType.SYNC -> "数据同步"
            BusinessType.OTHER -> "操作"
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
