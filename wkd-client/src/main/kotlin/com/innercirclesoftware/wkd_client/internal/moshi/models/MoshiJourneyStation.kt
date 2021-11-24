package com.innercirclesoftware.wkd_client.internal.moshi.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MoshiJourneyStation(
    val time: String,
    val station: String,
)