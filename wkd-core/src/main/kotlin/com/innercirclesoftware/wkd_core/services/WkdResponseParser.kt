package com.innercirclesoftware.wkd_core.services

import arrow.core.Either
import org.jsoup.nodes.Document
import java.time.Instant

internal interface WkdResponseParser {

    fun parseJourneySearchResponse(
        time: Instant,
        document: Document
    ): Either<JourneyResponseParseError, List<Pair<Instant, Instant>>>

}

internal sealed class JourneyResponseParseError {

    data class MalformedDocument(val message: String) : JourneyResponseParseError()
    data class IncorrectFormat(val value: String, val message: String, val cause: Throwable) : JourneyResponseParseError()
    data class Unknown(val message: String, val cause: Throwable) : JourneyResponseParseError()

}