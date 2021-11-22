package com.innercirclesoftware.wkd_server

import io.micronaut.runtime.Micronaut.build
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.servers.Server

@OpenAPIDefinition(
    servers = [Server(url = "http://localhost:8080")],
    info = Info(
        title = "WKD Backend",
        version = "1.0",
        description = "Backend which scrapes train journeys from https://wkd.com.pl",
        contact = Contact(
            url = "https://innercirclesoftware.com/",
            name = "Maciej Kaznowski",
            email = "maciej@kaznowski.com"
        )
    )
)
object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        build()
            .args(*args)
            .packages("com.innercirclesoftware")
            .eagerInitAnnotated(EagerInit::class.java)
            .start()
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class EagerInit