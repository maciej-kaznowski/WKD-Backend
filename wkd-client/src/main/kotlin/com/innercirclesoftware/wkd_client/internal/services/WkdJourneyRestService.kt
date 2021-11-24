package com.innercirclesoftware.wkd_client.internal.services

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import com.innercirclesoftware.wkd_api.services.WkdJourneyService
import com.innercirclesoftware.wkd_client.exceptions.WkdHttpException
import com.innercirclesoftware.wkd_client.internal.converters.toApi
import com.innercirclesoftware.wkd_client.internal.moshi.models.MoshiJourney
import com.innercirclesoftware.wkd_client.internal.retrofit.JourneyRetrofitService
import retrofit2.Response
import java.time.Instant

internal class WkdJourneyRestService(
    private val retrofitService: JourneyRetrofitService,
) : WkdJourneyService {

    override fun search(fromStation: Station, toStation: Station, time: Instant?): List<Journey> {
        val call = retrofitService.search(
            fromStation = fromStation.name,
            toStation = toStation.name,
            time = time,
        )
        val response: Response<List<MoshiJourney>> = call.execute()
        if (response.isSuccessful) {
            return requireNotNull(response.body()) {
                "Successful response but body is null"
            }.map { it.toApi() }
        }

        val errorCode = response.code()
        val errorBody: String? = response.errorBody()?.string()
        val message: String? = response.message()
        throw WkdHttpException(
            errorCode = errorCode,
            errorBody = errorBody,
            errorMessage = message,
        )
    }
}