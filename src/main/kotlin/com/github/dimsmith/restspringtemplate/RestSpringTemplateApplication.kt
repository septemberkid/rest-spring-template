package com.github.dimsmith.restspringtemplate

import leaf.LeafDb
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestSpringTemplateApplication {
    init {
        LeafDb.instance().testConfiguration()
    }
}

fun main(args: Array<String>) {
    runApplication<RestSpringTemplateApplication>(*args)
}
