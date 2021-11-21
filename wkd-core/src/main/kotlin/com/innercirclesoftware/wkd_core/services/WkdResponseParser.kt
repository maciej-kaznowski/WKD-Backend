package com.innercirclesoftware.wkd_core.services

import arrow.core.Either
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import org.jsoup.nodes.Document
import java.time.Instant

typealias SearchResponseJourneyBuilder = (searchTime: Instant, startStation: Station, endStation: Station) -> Journey

interface WkdResponseParser {

    fun parseJourneySearchResponse(document: Document): Either<JourneyResponseParseError, List<SearchResponseJourneyBuilder>>

}

sealed class JourneyResponseParseError {

    data class MalformedDocument(val message: String) : JourneyResponseParseError()
    data class IncorrectFormat(val value: String, val message: String, val cause: Throwable) :
        JourneyResponseParseError()

    data class Unknown(val message: String, val cause: Throwable) : JourneyResponseParseError()

}