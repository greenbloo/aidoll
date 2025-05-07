package com.digitalxiaoyao.userservice.presentation.advice

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException

/**
 * 将参数校验失败 (WebExchangeBindException) 转换成统一的 {code, message, data} JSON。
 */
@RestControllerAdvice("com.digitalxiaoyao.userservice.presentation") // 只拦截本服务的控制器
class ValidationErrorHandler {

    data class ErrorResponse(
        val code: Int,
        val message: String,
        val data: Map<String, String>
    )

    @ExceptionHandler(WebExchangeBindException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onBindException(ex: WebExchangeBindException): ErrorResponse {
        val fieldErrors = ex.bindingResult
            .fieldErrors
            .associate { it.field to (it.defaultMessage ?: "invalid") }

        return ErrorResponse(
            code = HttpStatus.BAD_REQUEST.value(),
            message = "参数校验失败",
            data = fieldErrors
        )
    }
}
