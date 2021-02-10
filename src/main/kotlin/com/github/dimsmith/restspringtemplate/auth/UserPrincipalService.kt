package com.github.dimsmith.restspringtemplate.auth

import com.github.dimsmith.restspringtemplate.models.dbo.UserDbo
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserPrincipalService : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val userDbo = UserDbo()
        val user = userDbo.findByUsername(username) ?: throw BadCredentialsException("Invalid username or password")
        return UserPrincipal(user)
    }

}