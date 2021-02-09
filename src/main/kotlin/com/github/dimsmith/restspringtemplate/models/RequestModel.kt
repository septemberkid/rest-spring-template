package com.github.dimsmith.restspringtemplate.models

data class LoginRequest(val username: String = "", val password: String = "")
data class JWTRequest(val token: String = "")