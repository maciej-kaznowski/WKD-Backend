package com.innercirclesoftware.wkd_server.controllers

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import com.innercirclesoftware.wkd_api.services.WkdJourneyService
import com.innercirclesoftware.wkd_core.health.HealthReportingService
import com.innercirclesoftware.wkd_core.services.JourneyService
import com.innercirclesoftware.wkd_core.services.WkdScrapeError
import com.innercirclesoftware.wkd_core.services.impl.JourneyServiceImpl
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@Controller("/journeys", produces = [MediaType.APPLICATION_JSON], consumes = [MediaType.APPLICATION_JSON])
class JourneyController @Inject constructor(
    private val journeyService: JourneyService,
    private val healthReportingService: HealthReportingService,
) : WkdJourneyService {

    private val logger = LoggerFactory.getLogger(JourneyServiceImpl::class.java)

    @OptIn(ExperimentalTime::class)
    @Get("/search")
    override fun search(
        @QueryValue("fromStation") fromStation: Station,
        @QueryValue("toStation") toStation: Station,
        @QueryValue("time") time: Instant?,
    ): List<Journey> {
        logger.info("Searching for journeys from=$fromStation to=$toStation at time=$time")
        return measureTimedValue {
            journeyService.searchJourneys(
                time = time ?: Instant.now(),
                fromStation = fromStation,
                toStation = toStation
            )
        }.let { (result, duration) ->
            result
                .tap { healthReportingService.reportSearchEndpointHit() }
                .tapLeft { healthReportingService.reportSearchEndpointHit(success = false) }
                .fold(
                    ifLeft = { error: WkdScrapeError ->
                        logger.warn("Scrape error for from=$fromStation, to=$toStation, time=$time, error=$error, duration=$duration")
                        throw IllegalStateException("Error parsing response: $error")
                    },
                    ifRight = {
                        logger.info("Journeys searched from=$fromStation to=$toStation at time=$time in $duration")
                        it
                    }
                )
        }
    }
}