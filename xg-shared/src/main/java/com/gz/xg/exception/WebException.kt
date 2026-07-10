package com.gz.xg.exception

/**
 * 业务异常类型。
 * 用于在业务流程中抛出可识别的错误码与错误消息。
 */
open class WebException : RuntimeException {

    /**
     * 业务错误码。
     */
    val code: Int = 10001

    /**
     * 创建空业务异常。
     */
    constructor() : super()

    /**
     * 根据错误消息创建业务异常。
     *
     * @param message 错误消息。
     */
    constructor(message: String?) : super(message)

    /**
     * 根据错误消息和原因创建业务异常。
     *
     * @param message 错误消息。
     * @param cause 异常原因。
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * 根据异常原因创建业务异常。
     *
     * @param cause 异常原因。
     */
    constructor(cause: Throwable?) : super(cause)

    /**
     * 完整参数构造。
     *
     * @param message 错误消息。
     * @param cause 异常原因。
     * @param enableSuppression 是否启用异常抑制。
     * @param writableStackTrace 是否生成可写堆栈。
     */
    protected constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}
