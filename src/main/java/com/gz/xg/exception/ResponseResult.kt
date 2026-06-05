package com.gz.xg.exception

/**
 * 统一响应对象。
 * 用于约定前后端交互的标准返回结构。
 */
class ResponseResult {

    /**
     * 获取业务状态码。
     *
     * @return 业务状态码。
     */
    /**
     * 设置业务状态码。
     *
     * @param code 业务状态码。
     */
    /**
     * 业务状态码。
     * 10000 表示成功，其它值表示失败。
     */
    var code: Int = 0

    /**
     * 获取响应消息。
     *
     * @return 响应消息。
     */
    /**
     * 设置响应消息。
     *
     * @param message 响应消息。
     */
    /**
     * 响应消息。
     */
    var message: String? = null

    /**
     * 获取请求是否成功。
     *
     * @return true 表示成功，false 表示失败。
     */
    /**
     * 设置请求是否成功。
     *
     * @param this.isSuccess 成功标识。
     */
    /**
     * 请求是否成功。
     */
    var isSuccess: Boolean = false

    /**
     * 获取业务响应数据。
     *
     * @return 业务响应数据。
     */
    /**
     * 设置业务响应数据。
     *
     * @param data 业务响应数据。
     */
    /**
     * 业务响应数据。
     */
    var data: Any? = null

    companion object {
        /**
         * 创建无数据的成功响应。
         *
         * @return 成功响应对象。
         */
        fun success(): ResponseResult {
            val result = ResponseResult()
            result.code = 10000
            result.message = "操作成功"
            result.isSuccess = true
            return result
        }

        /**
         * 创建带数据的成功响应。
         *
         * @param data 响应数据。
         * @return 成功响应对象。
         */
        fun success(data: Any?): ResponseResult {
            val result: ResponseResult = success()
            result.data = data
            return result
        }

        /**
         * 创建失败响应。
         *
         * @param errMsg 错误消息。
         * @param code 业务错误码。
         * @return 失败响应对象。
         */
        fun fail(errMsg: String?, code: Int): ResponseResult {
            val result = ResponseResult()
            result.isSuccess = false
            result.code = code
            result.message = errMsg
            return result
        }

        /**
         * 根据业务异常创建失败响应。
         *
         * @param e 业务异常对象。
         * @return 失败响应对象。
         */
        fun fail(e: WebException): ResponseResult {
            return fail(e.message, e.code)
        }

        /**
         * 根据系统异常创建失败响应。
         *
         * @param e 系统异常对象。
         * @return 失败响应对象，错误码固定为 99999。
         */
        fun fail(e: Exception?): ResponseResult {
            return fail("系统异常", 99999)
        }
    }
}
