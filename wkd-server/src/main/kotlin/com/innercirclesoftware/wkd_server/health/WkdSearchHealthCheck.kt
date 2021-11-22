package com.innercirclesoftware.wkd_server.health

import arrow.core.filterOrElse
import arrow.core.getOrHandle
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import com.innercirclesoftware.wkd_core.Wkd
import com.innercirclesoftware.wkd_core.services.JourneyService
import com.innercirclesoftware.wkd_server.EagerInit
import io.micronaut.context.annotation.Requires
import io.micronaut.management.endpoint.info.InfoEndpoint
import io.micronaut.management.health.indicator.AbstractHealthIndicator
import io.micronaut.runtime.context.scope.Refreshable
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.time.Instant
import java.time.LocalDateTime

@Singleton
@EagerInit
@Refreshable
@Requires(beans = [InfoEndpoint::class])
class WkdSearchHealthCheck @Inject constructor(
    private val journeyService: JourneyService
) : AbstractHealthIndicator<List<Journey>>() {

    private val time: Instant = LocalDateTime.of(2021, 11, 22, 9, 0).atZone(Wkd.TIMEZONE).toInstant()
    private val from: Station = Station.GRODZISK_MAZ_RADONSKA
    private val to: Station = Station.WARSZAWA_SRODMIESCIE_WKD

    override fun getHealthInformation(): List<Journey> {
        return journeyService.searchJourneys(time, from, to)
            .mapLeft { error -> "Failed to search journeys: $error" }
            .filterOrElse({ journeys -> journeys.isNotEmpty() }) { "No journeys returned" }
            .getOrHandle { error -> throw IllegalStateException(error) }
    }

    override fun getName(): String = "Search scraping"
}