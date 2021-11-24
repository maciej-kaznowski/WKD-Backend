package com.innercirclesoftware.wkd_client.internal.converters

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.JourneyStation
import com.innercirclesoftware.wkd_client.exceptions.EnumNotFoundException
import com.innercirclesoftware.wkd_client.internal.moshi.models.MoshiJourney
import com.innercirclesoftware.wkd_client.internal.moshi.models.MoshiJourneyStation
import java.time.Instant

internal fun MoshiJourney.toApi(): Journey {
    return Journey(
        start = start.toApi(),
        end = end.toApi(),
        distanceMeters = distanceMeters,
        journeyOperationMode = journeyOperationMode.toEnumOrThrow(),
    )
}

internal fun MoshiJourneyStation.toApi(): JourneyStation {
    return JourneyStation(
        time = Instant.parse(time),
        station = station.toEnumOrThrow()
    )
}

private inline fun <reified E : Enum<E>> String.toEnumOrThrow(): E {
    val values = E::class.java.enumConstants
    return values.firstOrNull { it.name == this } ?: throw EnumNotFoundException(
        clazz = E::class.java,
        name = this,
        values = values,
    )
}

