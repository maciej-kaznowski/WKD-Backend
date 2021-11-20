package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.Either
import arrow.core.filterOrOther
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.JourneyStation
import com.innercirclesoftware.wkd_core.Wkd
import com.innercirclesoftware.wkd_core.services.JourneyService
import com.innercirclesoftware.wkd_core.services.WkdResponseParser
import com.innercirclesoftware.wkd_core.services.WkdScrapeError
import com.innercirclesoftware.wkd_core.utils.Jsoup
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.*
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZonedDateTime


@Singleton
class JourneyServiceImpl @Inject internal constructor(
    private val okHttpClient: OkHttpClient,
    private val wkdResponseParser: WkdResponseParser,
) : JourneyService {

    override fun searchJourneys(
        time: Instant,
        fromStationId: Long,
        toStationId: Long,
    ): Either<WkdScrapeError, List<Journey>> {
        val request = buildSearchRequest(time = time, fromStationId = fromStationId, toStationId = toStationId)

        return Either
            .Right(okHttpClient.newCall(request))
            .flatMap { call ->
                Either
                    .catch { call.execute() }
                    .mapLeft { throwable -> WkdScrapeError.HttpError("Error executing request", throwable) }
                    .filterOrOther(Response::isSuccessful) { response ->
                        WkdScrapeError.HttpError(
                            "Response not successful: $response",
                            null
                        )
                    }
            }
            .flatMap { response ->
                response.body.rightIfNotNull<WkdScrapeError, ResponseBody> {
                    WkdScrapeError.HttpError("No body", null)
                }
            }
            .flatMap { body ->
                Either
                    .catch { body.use { Jsoup.requireParse(it.byteStream(), StandardCharsets.UTF_8, request.url) } }
                    .mapLeft<WkdScrapeError> { throwable: Throwable ->
                        return@mapLeft WkdScrapeError.HttpError("Problem parsing body with jsoup", cause = throwable)
                    }
            }
            .flatMap { document ->
                wkdResponseParser
                    .parseJourneySearchResponse(time, document)
                    .mapLeft { journeyResponseParseError ->
                        WkdScrapeError.ResponseParseError(
                            message = "Error parsing document response for time=$time, fromStationId=$fromStationId, toStationId=$toStationId: error=$journeyResponseParseError",
                        )
                    }
            }
            .map { journeyTimes ->
                journeyTimes.map { (startTime, endTime) ->
                    val startStation = JourneyStation(
                        time = startTime,
                        stationId = fromStationId,
                    )
                    val endStation = JourneyStation(
                        time = endTime,
                        stationId = toStationId,
                    )

                    Journey(start = startStation, end = endStation)
                }
            }
    }

    private fun buildSearchRequest(
        time: Instant,
        fromStationId: Long,
        toStationId: Long
    ): Request {
        val localTime = ZonedDateTime.ofInstant(time, Wkd.TIMEZONE)
        val timetableDate = Wkd.DATE_PATTERN.format(localTime)
        val timetableTime = Wkd.TIME_PATTERN.format(localTime)
        val requestBody: RequestBody = FormBody.Builder(StandardCharsets.UTF_8)
            .addEncoded("timetable_date", timetableDate)
            .addEncoded("timetable_time", timetableTime)
            .addEncoded("timetable_from", fromStationId.toString())
            .addEncoded("timetable_to", toStationId.toString())
            .addEncoded("search", "1")
            .build()

        return Request.Builder()
            .url("https://wkd.com.pl/rozklad-jazdy")
            .post(requestBody)
            .build()
    }
}