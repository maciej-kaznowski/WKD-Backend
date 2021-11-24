package com.innercirclesoftware.wkd_server.controllers

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import com.innercirclesoftware.wkd_api.services.WkdJourneyService
import com.innercirclesoftware.wkd_core.services.JourneyService
import com.innercirclesoftware.wkd_core.services.WkdScrapeError
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject
import java.time.Instant

@Controller("/journeys", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.APPLICATION_JSON])
class JourneyController @Inject constructor(
    private val journeyService: JourneyService
) : WkdJourneyService {

    @Get("/search")
    override fun search(
        @QueryValue("fromStation") fromStation: Station,
        @QueryValue("toStation") toStation: Station,
        @QueryValue("time") time: Instant?,
    ): List<Journey> = journeyService.searchJourneys(
        time = time ?: Instant.now(),
        fromStation = fromStation,
        toStation = toStation
    ).fold(
        ifLeft = { error: WkdScrapeError -> throw IllegalStateException("Error parsing response: $error") },
        ifRight = { it }
    )
}