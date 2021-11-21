package com.innercirclesoftware.wkd_core.services

import arrow.core.Either
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import java.time.Instant

interface JourneyService {

    fun searchJourneys(
        time: Instant,
        fromStation: Station,
        toStation: Station,
    ): Either<WkdScrapeError, List<Journey>>

}

sealed class WkdScrapeError {

    data class HttpError(val message: String, val cause: Throwable?) : WkdScrapeError()
    data class ResponseParseError(val message: String) : WkdScrapeError()

}