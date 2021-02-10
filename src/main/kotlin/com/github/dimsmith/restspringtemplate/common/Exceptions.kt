package com.github.dimsmith.restspringtemplate.common

import org.springframework.http.HttpStatus

sealed class RestException(val title: String, override val message: String, val status: HttpStatus) : RuntimeException()
class AuthException(message: String) : RestException(ResponseTitle.INVALID_AUTHENTICATION.desc, message, HttpStatus.UNAUTHORIZED)
class MyForbiddenException(message: String) : RestException(ResponseTitle.RESTRICTED_ACCESS.desc, message, HttpStatus.FORBIDDEN)
class JWTException(message: String) : RestException(ResponseTitle.INVALID_AUTHENTICATION.desc, message, HttpStatus.INTERNAL_SERVER_ERROR)