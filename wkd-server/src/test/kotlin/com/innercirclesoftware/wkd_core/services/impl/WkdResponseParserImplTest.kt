package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.getOrHandle
import com.innercirclesoftware.wkd_core.services.WkdResponseParser
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.time.Instant

@MicronautTest
class WkdResponseParserImplTest {

    @Inject
    internal lateinit var wkdResponseParser: WkdResponseParser

    @Test
    fun `should parse correctly`() {
        val response: InputStream = WkdResponseParserImplTest::class.java.getResourceAsStream("wkd_search_journeys_success.html")!!
        val document = Jsoup.parse(response.bufferedReader().readText(), StandardCharsets.UTF_8.name())
        val result = wkdResponseParser.parseJourneySearchResponse(Instant.now(), document)
        val journeys = result.getOrHandle { error ->
            fail { "error: $error" }
        }
        Assertions.assertTrue(journeys.isNotEmpty())
    }

}