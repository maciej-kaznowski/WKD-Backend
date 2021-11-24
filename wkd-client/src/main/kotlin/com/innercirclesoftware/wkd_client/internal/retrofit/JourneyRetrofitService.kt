package com.innercirclesoftware.wkd_client.internal.retrofit

import com.innercirclesoftware.wkd_client.internal.moshi.models.MoshiJourney
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.time.Instant

internal interface JourneyRetrofitService {

    @GET("journeys/search")
    fun search(
        @Query("fromStation") fromStation: String,
        @Query("toStation") toStation: String,
        @Query("time") time: Instant?,
    ): Call<List<MoshiJourney>>

}