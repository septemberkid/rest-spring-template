package com.github.dimsmith.restspringtemplate.common

import org.springframework.http.HttpStatus

sealed class RestException(val title: String, override val message: String, val status: HttpStatus) : RuntimeException()
class AuthException(message: String) : RestException("Authentication Failed", message, HttpStatus.UNAUTHORIZED)
class JWTException(message: String) : RestException("JWT Failed", message, HttpStatus.INTERNAL_SERVER_ERROR)