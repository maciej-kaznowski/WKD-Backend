package com.innercirclesoftware.wkd_server.config

import io.micronaut.core.annotation.TypeHint
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.convert.TypeConverterRegistrar
import jakarta.inject.Singleton
import java.time.Instant

@Singleton
@TypeHint(
    value = [Instant::class],
    accessType = [TypeHint.AccessType.ALL_PUBLIC]
)
class WkdTypeConverterRegistrar : TypeConverterRegistrar {

    /**
     * Add basic support for converting `Instant` using ISO-8601 specification.
     *
     * Micronaut by default doesn't support Instant as a convertable type (see `io.micronaut.runtime.converters.time.TimeConverterRegistrar`)
     */
    override fun register(conversionService: ConversionService<*>) {
        conversionService.addConverter(
            Instant::class.java,
            String::class.java,
        ) { instant: Instant? -> instant.toString() }
        conversionService.addConverter(
            String::class.java,
            Instant::class.java,
        ) { instantStr: String? -> Instant.parse(instantStr) }
    }
}