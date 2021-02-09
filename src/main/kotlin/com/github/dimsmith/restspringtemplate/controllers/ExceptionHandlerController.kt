package com.github.dimsmith.restspringtemplate.controllers

import com.github.dimsmith.restspringtemplate.common.RestException
import com.github.dimsmith.restspringtemplate.models.RestErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionHandlerController : ResponseEntityExceptionHandler() {

    @ExceptionHandler(RestException::class)
    protected fun handleAuthException(ex: RestException): ResponseEntity<RestErrorResponse> {
        return ResponseEntity(
            RestErrorResponse(
                ex.status.value(),
                ex.title,
                ex.message
            ), ex.status
        )
    }


    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return ResponseEntity(RestErrorResponse(status.value(), status.reasonPhrase, ex.message ?: ""), status)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return ResponseEntity(RestErrorResponse(status.value(), status.reasonPhrase, ex.message ?: ""), status)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        return ResponseEntity(RestErrorResponse(status.value(), status.reasonPhrase, ex.message ?: ""), status)
    }
}