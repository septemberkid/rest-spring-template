package com.github.dimsmith.restspringtemplate.models

data class RestResponse(val statusCode: Int, val data: Any?)
data class RestErrorResponse(val statusCode: Int, val title: String, val detail: String)
data class RestAuthResponse(val statusCode: Int, val token: String)