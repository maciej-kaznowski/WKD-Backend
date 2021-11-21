package com.innercirclesoftware.wkd_server.config

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.jackson.JacksonConfiguration
import io.micronaut.jackson.ObjectMapperFactory

@Factory
@Replaces(ObjectMapperFactory::class)
class WkdObjectMapperFactory : ObjectMapperFactory() {

    override fun objectMapper(jacksonConfiguration: JacksonConfiguration?, jsonFactory: JsonFactory?): ObjectMapper {
        return super.objectMapper(jacksonConfiguration, jsonFactory).apply {
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }
}