package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.Either
import arrow.core.filterOrOther
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.Station
import com.innercirclesoftware.wkd_core.Wkd
import com.innercirclesoftware.wkd_core.models.stationId
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
import java.time.format.DateTimeFormatter

private val SEARCH_TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("HH'+':'+'mm")
private val SEARCH_DATE_PATTERN: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

@Singleton
class JourneyServiceImpl @Inject internal constructor(
    private val okHttpClient: OkHttpClient,
    private val wkdResponseParser: WkdResponseParser,
) : JourneyService {

    override fun searchJourneys(
        time: Instant,
        fromStation: Station,
        toStation: Station,
    ): Either<WkdScrapeError, List<Journey>> {
        val request = buildSearchRequest(time = time, fromStation = fromStation, toStation = toStation)

        return Either
            .Right(okHttpClient.newCall(request))
            .flatMap { call ->
                Either
                    .catch { call.execute() }
                    .mapLeft { throwable -> WkdScrapeError.HttpError("Error executing request", throwable) }
                    .filterOrOther(Response::isSuccessful) { response ->
                        WkdScrapeError.HttpError("Response not successful: $response", null)
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
                    .mapLeft<WkdScrapeError> { throwable ->
                        WkdScrapeError.HttpError("Problem parsing body with jsoup", throwable)
                    }
            }
            .flatMap { document ->
                wkdResponseParser
                    .parseJourneySearchResponse(document)
                    .mapLeft { journeyResponseParseError ->
                        WkdScrapeError.ResponseParseError(
                            message = "Error parsing document response for time=$time, fromStation=$fromStation, toStation=$toStation: error=$journeyResponseParseError",
                        )
                    }
            }
            .map { journeys ->
                journeys.map { journeyFromSearchResponse -> journeyFromSearchResponse(time, fromStation, toStation) }
            }
    }

    private fun buildSearchRequest(
        time: Instant,
        fromStation: Station,
        toStation: Station,
    ): Request {
        val localTime = ZonedDateTime.ofInstant(time, Wkd.TIMEZONE)
        val timetableDate = SEARCH_DATE_PATTERN.format(localTime)
        val timetableTime = SEARCH_TIME_PATTERN.format(localTime)

        val requestBody: RequestBody = FormBody.Builder(StandardCharsets.UTF_8)
            .addEncoded("timetable_date", timetableDate)
            .addEncoded("timetable_time", timetableTime)
            .addEncoded("timetable_from", fromStation.stationId.id.toString())
            .addEncoded("timetable_to", toStation.stationId.id.toString())
            .addEncoded("search", "1")
            .build()

        return Request.Builder()
            .url("https://wkd.com.pl/rozklad-jazdy")
            .post(requestBody)
            .build()
    }
}