package com.innercirclesoftware.wkd_api.models

data class Journey(
    val start: JourneyStation,
    val end: JourneyStation,
    val distanceMeters: Int,
    val journeyOperationMode: JourneyOperationMode,
)