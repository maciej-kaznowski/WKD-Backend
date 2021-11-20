package com.innercirclesoftware.wkd_core.services

import com.innercirclesoftware.wkd_api.models.Journey
import java.time.Instant

interface JourneyService {

    fun searchJourneys(
        time: Instant,
        fromStationId: Long,
        toStationId: Long
    ): List<Journey>

}