package com.innercirclesoftware.wkd_client.internal.moshi.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MoshiJourney(
    val start: MoshiJourneyStation,
    val end: MoshiJourneyStation,
    val distanceMeters: Int,
    val journeyOperationMode: String,
)