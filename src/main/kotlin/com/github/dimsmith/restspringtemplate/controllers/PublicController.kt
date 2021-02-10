package com.github.dimsmith.restspringtemplate.controllers

import com.github.dimsmith.restspringtemplate.auth.JWTFactory
import com.github.dimsmith.restspringtemplate.auth.UserPrincipalService
import com.github.dimsmith.restspringtemplate.common.AuthException
import com.github.dimsmith.restspringtemplate.common.JWTException
import com.github.dimsmith.restspringtemplate.models.JWTRequest
import com.github.dimsmith.restspringtemplate.models.LoginRequest
import com.github.dimsmith.restspringtemplate.models.RestAuthResponse
import com.github.dimsmith.restspringtemplate.models.RestResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.*

@RestController
class PublicController(
    val authenticationManager: AuthenticationManager,
    val userPrincipalService: UserPrincipalService
) {

    @GetMapping("/")
    fun index(): ResponseEntity<Any> {
        return ResponseEntity(RestResponse(HttpStatus.OK.value(), "Service is online"), HttpStatus.OK)
    }

    @RequestMapping(path = ["/auth/create-token"], method = [RequestMethod.POST])
    @Throws(AuthException::class, IllegalArgumentException::class)
    fun createToken(
        @RequestBody reqBody: LoginRequest
    ): ResponseEntity<Any> {
        Assert.hasText(reqBody.username, "Username is required!")
        Assert.hasText(reqBody.password, "Password is required!")
        try {

            authenticationManager.authenticate(UsernamePasswordAuthenticationToken(reqBody.username, reqBody.password))
            val userDetail = userPrincipalService.loadUserByUsername(reqBody.username)
            val token = JWTFactory.produce(
                "authentication", mapOf(
                    "userDetail" to userDetail
                )
            )
            return ResponseEntity(RestAuthResponse(HttpStatus.OK.value(), token), HttpStatus.OK)
        } catch (e: Exception) {
            throw AuthException(e.message ?: "Invalid username or password")
        }
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
