package com.github.dimsmith.restspringtemplate.controllers

import com.github.dimsmith.restspringtemplate.common.AuthException
import com.github.dimsmith.restspringtemplate.common.JWTException
import com.github.dimsmith.restspringtemplate.common.JWTFactory
import com.github.dimsmith.restspringtemplate.models.JWTRequest
import com.github.dimsmith.restspringtemplate.models.LoginRequest
import com.github.dimsmith.restspringtemplate.models.RestAuthResponse
import com.github.dimsmith.restspringtemplate.models.RestResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
class PublicController {

    @GetMapping("/")
    fun index(): ResponseEntity<Any> {
        return ResponseEntity(RestResponse(HttpStatus.OK.value(), "Service is online"), HttpStatus.OK)
    }

    @RequestMapping(path = ["/auth/create-token"], method = [RequestMethod.POST])
    @Throws(AuthException::class)
    fun createToken(
        @RequestBody reqBody: LoginRequest,
        request: HttpServletRequest
    ): ResponseEntity<Any> {
        if (!reqBody.username.equals("dimsmith") || !reqBody.password.equals("dimsmith"))
            throw AuthException("Invalid username or password")
        val token = JWTFactory.produce(
            "authentication", mapOf(
                "username" to reqBody.username,
                "api_key" to request.getHeader("x-api-key")
            )
        )
        return ResponseEntity(RestAuthResponse(HttpStatus.OK.value(), token), HttpStatus.OK)
    }

    @RequestMapping(path = ["/auth/claim-token"], method = [RequestMethod.POST])
    @Throws(JWTException::class)
    fun claim(
        @RequestBody reqBody: JWTRequest
    ): ResponseEntity<Any> {
        if (reqBody.token.isEmpty())
            throw JWTException("Token is required")
        val token = JWTFactory.claim(reqBody.token)
        return ResponseEntity(RestResponse(HttpStatus.OK.value(), token), HttpStatus.OK)
    }
}
