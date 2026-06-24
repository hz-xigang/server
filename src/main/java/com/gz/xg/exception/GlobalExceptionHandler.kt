package com.gz.xg.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.function.Consumer

/**
 * 全局异常处理器。
 * 统一拦截控制层抛出的异常，并转换为标准响应结构。
 */
@RestControllerAdvice
open class GlobalExceptionHandler {
    /**
     * 处理业务异常。
     *
     * @param e 业务异常对象。
     * @return 失败响应。
     */
    @ExceptionHandler(WebException::class)
    fun webException(e: WebException): ResponseResult {
        return ResponseResult.fail(e)
    }

    /**
     * 处理请求参数校验异常。
     *
     * @param exception 参数校验异常对象。
     * @return 失败响应，错误码为 10003。
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun validationBodyException(exception: MethodArgumentNotValidException): ResponseResult {
        val bindingResult: BindingResult = exception.bindingResult
        val fieldErrors: MutableList<FieldError?> = bindingResult.fieldErrors
        val sb = StringBuilder()
        fieldErrors.forEach(Consumer { p: FieldError? -> sb.append(p?.defaultMessage).append(",") })
        val errMsg = sb.deleteCharAt(sb.length - 1).toString()

        LOGGER.error(errMsg)
        return ResponseResult.fail(errMsg, 10003)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(e: HttpMessageNotReadableException) : ResponseResult {
        val msg = when (val cause = e.cause) {
            is com.fasterxml.jackson.core.JsonParseException ->
                "JSON格式错误"
            is com.fasterxml.jackson.databind.exc.MismatchedInputException ->
                "JSON字段类型不匹配"
            else ->
                "请求参数无法解析"
        }
        e.printStackTrace()
        return ResponseResult.fail( msg, 10003)
    }


    /**
     * 处理兜底异常。
     *
     * @param e 未分类异常对象。
     * @return 失败响应，错误码为 99999。
     */
    @ExceptionHandler(Exception::class)
    fun finalException(e: Exception): ResponseResult {
        LOGGER.error("exception type==={}", e.javaClass)
        //logSysExceptionMsg(e)
        e.printStackTrace()
        return ResponseResult.fail(e)
    }

    /**
     * 记录异常详情日志。
     *
     * @param e 异常对象。
     */
    private fun logSysExceptionMsg(e: Exception) {
        LOGGER.error(e.localizedMessage)
        val sb = StringBuilder()
        val stackArray = e.stackTrace
        for (element in stackArray) {
            sb.append(element.toString()).append("\n")
        }
        LOGGER.error("\n" + sb.toString())
    }

    companion object {
        /**
         * 异常处理日志记录器。
         */
        private val LOGGER: Logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}
