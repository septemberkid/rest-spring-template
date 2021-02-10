package com.github.dimsmith.restspringtemplate.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dimsmith.restspringtemplate.models.RestErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import javax.servlet.http.HttpServletResponse

fun HttpServletResponse.sendCustomResponse(
    title: String,
    ex: Exception,
    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    contentType: MediaType = MediaType.APPLICATION_JSON
) {
    val mapper = ObjectMapper()
    this.status = httpStatus.value()
    this.setHeader("Content-Type", contentType.toString())
    mapper.writeValue(
        this.writer,
        RestErrorResponse(
            httpStatus.value(),
            title,
            ex.message ?: ""
        )
    )
}

fun String.test() {}