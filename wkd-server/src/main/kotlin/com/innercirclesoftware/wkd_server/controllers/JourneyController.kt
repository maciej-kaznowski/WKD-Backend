package com.innercirclesoftware.wkd_server.controllers

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_core.services.JourneyService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject
import java.time.Instant

@Controller("/journeys", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.APPLICATION_JSON])
class JourneyController @Inject constructor(
    private val journeyService: JourneyService
) {

    @Get("/search")
    fun search(
        @QueryValue("fromStationId") fromStationId: Long,
        @QueryValue("toStationId") toStationId: Long,
        @QueryValue("time") time: Instant?,
    ): List<Journey> = journeyService.searchJourneys(
        time = time ?: Instant.now(),
        fromStationId = fromStationId,
        toStationId = toStationId,
    )
}