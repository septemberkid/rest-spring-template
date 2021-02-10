package com.github.dimsmith.restspringtemplate.auth

import com.github.dimsmith.restspringtemplate.common.JWTException
import com.github.dimsmith.restspringtemplate.common.ResponseTitle
import com.github.dimsmith.restspringtemplate.common.sendCustomResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JWTFilter(private val userPrincipalService: UserPrincipalService) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        val jwt: String
        val jwtClaims: Map<String, Any>
        var username: String? = null
        if (!authorizationHeader.isNullOrEmpty() && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7)
            try {
                jwtClaims = JWTFactory.claim(jwt)
                username = jwtClaims["username"].toString()
            } catch (e: JWTException) {
                response.sendCustomResponse(ResponseTitle.INVALID_AUTHENTICATION.desc, e, HttpStatus.UNAUTHORIZED)
            }
        }
        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            val userDetail = userPrincipalService.loadUserByUsername(username)
            val usernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken(userDetail, null, userDetail.authorities)
            usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        }
        filterChain.doFilter(request, response)
    }
}