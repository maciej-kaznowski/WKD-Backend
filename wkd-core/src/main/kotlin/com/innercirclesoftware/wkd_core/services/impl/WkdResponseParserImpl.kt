package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.traverseEither
import com.innercirclesoftware.wkd_core.Wkd
import com.innercirclesoftware.wkd_core.services.JourneyResponseParseError
import com.innercirclesoftware.wkd_core.services.WkdResponseParser
import com.innercirclesoftware.wkd_core.utils.requireElementsByClass
import com.innercirclesoftware.wkd_core.utils.requireOwnText
import com.innercirclesoftware.wkd_core.utils.requireSelect
import jakarta.inject.Singleton
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoField

@Singleton
internal class WkdResponseParserImpl : WkdResponseParser {

    override fun parseJourneySearchResponse(
        time: Instant,
        document: Document
    ): Either<JourneyResponseParseError, List<Pair<Instant, Instant>>> {
        return Either.catch { document.requireElementsByClass("train") }
            .mapLeft { throwable ->
                when (throwable) {
                    is NullPointerException -> JourneyResponseParseError.MalformedDocument(""""train" class not found""")
                    else -> JourneyResponseParseError.Unknown("""Unknown error getting elements by class "train"""", throwable)
                }
            }
            .flatMap { trains: Elements ->
                trains.traverseEither { trainElement ->
                    Either.catch {
                        trainElement.requireSelect(".train-time")
                    }.mapLeft { throwable ->
                        when (throwable) {
                            is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No elements for css selector ".train-time in $trainElement"""")
                            else -> JourneyResponseParseError.Unknown(
                                """Unknown error getting elements for css selector ".train-time" in $trainElement""",
                                throwable
                            )
                        }
                    }
                }
            }
            .flatMap { trainTimes: List<Elements> ->
                val (correctSize, incorrectSize) = trainTimes.partition { it.size == 2 }
                if (incorrectSize.isNotEmpty()) {
                    val msg = "Incorrect number of train times. correct=${correctSize.size}, incorrect=$incorrectSize"
                    Either.Left(JourneyResponseParseError.MalformedDocument(msg))
                } else {
                    val timeElements = correctSize.map { times -> times[0] to times[1] }
                    Either.Right(timeElements)
                }
            }
            .flatMap { timeElements ->
                timeElements.traverseEither { (startTimeElement, endTimeElement) ->
                    Either.catch { startTimeElement.requireOwnText() }
                        .flatMap { starTimeStr ->
                            Either.catch { endTimeElement.requireOwnText() }
                                .map { startTimeEnd -> starTimeStr to startTimeEnd }
                        }
                        .mapLeft { throwable ->
                            when (throwable) {
                                is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No ownText (startTimeElement, endTimeElement)=($startTimeElement, $endTimeElement)"""")
                                else -> JourneyResponseParseError.Unknown(
                                    """Unknown error getting ownText for ownText (startTimeElement, endTimeElement)=($startTimeElement, $endTimeElement)""",
                                    throwable
                                )
                            }
                        }
                        .flatMap { (startTimeStr, endTimeStr) ->
                            parseTime(time = time, startTime = startTimeStr, endTime = endTimeStr)
                        }
                }
            }
    }

    private fun parseTime(
        time: Instant,
        startTime: String,
        endTime: String
    ): Either<JourneyResponseParseError.IncorrectFormat, Pair<Instant, Instant>> {

        fun <T> Either<Throwable, T>.mapLeftToElementParsingError(
            valueDescription: String,
            rawValue: String
        ): Either<JourneyResponseParseError.IncorrectFormat, T> {
            return mapLeft { throwable ->
                JourneyResponseParseError.IncorrectFormat(
                    value = rawValue,
                    message = "Could not parse $valueDescription=$rawValue with format=${Wkd.TIME_PATTERN}",
                    cause = throwable,
                )
            }
        }

        val startEither = Either.catch {
            parseTimeRawValue(time, startTime)
        }.mapLeftToElementParsingError("startTime", startTime)
        val endEither = Either.catch {
            parseTimeRawValue(time, endTime)
        }.mapLeftToElementParsingError("endTime", endTime)

        return startEither.flatMap { start -> endEither.map { end -> start to end } }
    }

    private fun parseTimeRawValue(
        time: Instant,
        timeStr: String,
    ): Instant {
        val temporalAccessor = Wkd.TIME_PATTERN.parse(timeStr)
        return instantWithLocalTime(
            time,
            hour = temporalAccessor.get(ChronoField.HOUR_OF_DAY),
            minute = temporalAccessor.get(ChronoField.MINUTE_OF_HOUR)
        )
    }

    private fun instantWithLocalTime(time: Instant, hour: Int, minute: Int): Instant {
        return ZonedDateTime.ofInstant(time, Wkd.TIMEZONE)
            .withHour(hour)
            .withMinute(minute)
            .toInstant()
    }


}