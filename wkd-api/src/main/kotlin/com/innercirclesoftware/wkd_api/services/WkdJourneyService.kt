package com.innercirclesoftware.wkd_api.services

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import java.time.Instant

interface WkdJourneyService {

    /**
     * Search for journeys.
     *
     * @param fromStation The station we are travelling from
     * @param toStation The station we are travelling to
     * @param time The optional time we wish to depart after. Defaults to now.
     */
    fun search(
        fromStation: Station,
        toStation: Station,
        time: Instant?,
    ): List<Journey>

}