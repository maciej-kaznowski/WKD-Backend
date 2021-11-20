package com.innercirclesoftware.wkd_api.models

import java.time.Instant

data class JourneyStation(
    val time: Instant,
    val stationId: Long,
)