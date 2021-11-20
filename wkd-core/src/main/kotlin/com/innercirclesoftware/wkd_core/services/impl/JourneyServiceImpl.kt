package com.innercirclesoftware.wkd_core.services.impl

import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.JourneyStation
import com.innercirclesoftware.wkd_core.services.JourneyService
import jakarta.inject.Inject
import jakarta.inject.Singleton
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

private val WARSAW_TIMEZONE = ZoneId.of("Europe/Warsaw")

@Singleton
class JourneyServiceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : JourneyService {

    override fun searchJourneys(
        time: Instant,
        fromStationId: Long,
        toStationId: Long,
    ): List<Journey> {
        val request = buildSearchRequest(time = time, fromStationId = fromStationId, toStationId = toStationId)
        val response = okHttpClient.newCall(request).execute()
        return response.body!!.use { body ->
            val parsedResponse = Jsoup.parse(body.byteStream(), StandardCharsets.UTF_8.name(), request.url.toString())
            val trains: Elements = parsedResponse.getElementsByClass("train")

            trains.map { trainElement ->
                val trainTimes = trainElement.select(".train-time")
                require(trainTimes.size == 2) { trainTimes.size }
                val (startTrainTimeElement, endTrainTimeElement) = trainTimes.map { it.text() }
                val timePattern = DateTimeFormatter.ofPattern("HH:mm")
                val startTimeTemporal = timePattern.parse(startTrainTimeElement)
                val endTimeTemporal = timePattern.parse(endTrainTimeElement)
                val startTime = instantWithLocalTime(
                    time,
                    hour = startTimeTemporal.get(ChronoField.HOUR_OF_DAY),
                    minute = startTimeTemporal.get(ChronoField.MINUTE_OF_HOUR)
                )
                val endTime = instantWithLocalTime(
                    time,
                    hour = endTimeTemporal.get(ChronoField.HOUR_OF_DAY),
                    minute = endTimeTemporal.get(ChronoField.MINUTE_OF_HOUR)
                )
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

    private fun instantWithLocalTime(time: Instant, hour: Int, minute: Int): Instant {
        return ZonedDateTime.ofInstant(time, WARSAW_TIMEZONE)
            .withHour(hour)
            .withMinute(minute)
            .toInstant()
    }


    private fun buildSearchRequest(
        time: Instant,
        fromStationId: Long,
        toStationId: Long
    ): Request {
        val localTime = ZonedDateTime.ofInstant(time, WARSAW_TIMEZONE)
        val timetableDate = DateTimeFormatter.ISO_LOCAL_DATE.format(localTime)
        val timetableTime = DateTimeFormatter.ofPattern("HH:mm").format(localTime)
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