package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.*
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.JourneyStation
import com.innercirclesoftware.wkd_core.Wkd.withLocalTime
import com.innercirclesoftware.wkd_core.services.JourneyResponseParseError
import com.innercirclesoftware.wkd_core.services.SearchResponseJourneyBuilder
import com.innercirclesoftware.wkd_core.services.WkdResponseParser
import com.innercirclesoftware.wkd_core.utils.requireElementsByClass
import com.innercirclesoftware.wkd_core.utils.requireSelect
import jakarta.inject.Singleton
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Returns a function which can be invoked with the station names and time the search took place to build a Journey
 */
private val parsedSearchResponse: (
    start: LocalTime,
    end: LocalTime,
    distanceMeters: Int,
) -> SearchResponseJourneyBuilder = { start, end, distanceMeters ->
    { searchTime, startStation, endStation ->
        Journey(
            start = JourneyStation(time = searchTime.withLocalTime(start), station = startStation),
            end = JourneyStation(time = searchTime.withLocalTime(end), station = endStation),
            distanceMeters = distanceMeters
        )
    }
}

private val TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Singleton
class WkdResponseParserImpl : WkdResponseParser {

    override fun parseJourneySearchResponse(document: Document): Either<JourneyResponseParseError, List<SearchResponseJourneyBuilder>> {
        return Either.catch { document.requireElementsByClass("train") }
            .mapLeft { throwable ->
                when (throwable) {
                    is NullPointerException -> JourneyResponseParseError.MalformedDocument(""""train" class not found""")
                    else -> JourneyResponseParseError.Unknown(
                        message = """Unknown error getting elements by class "train"""",
                        cause = throwable
                    )
                }
            }
            .flatMap { trains -> trains.traverseEither { train -> parseTrain(train) } }
    }

    private val parseTrain: (Element) -> Either<JourneyResponseParseError, SearchResponseJourneyBuilder> = { train ->
        parseStartTime(train)
            .zip(parseEndTime(train), parseDistanceMeters(train)) { start, end, distanceMeters ->
                parsedSearchResponse(start, end, distanceMeters)
            }
    }
    private val parseStartTime: (Element) -> Either<JourneyResponseParseError, LocalTime> =
        { parseTimeForType(it, "Odjazd") }
    private val parseEndTime: (Element) -> Either<JourneyResponseParseError, LocalTime> =
        { parseTimeForType(it, "Przyjazd") }

    private fun parseTimeForType(train: Element, type: String): Either<JourneyResponseParseError, LocalTime> = Either
        .catch { train.requireSelect(".center") }
        .mapLeft { throwable ->
            when (throwable) {
                is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No elements for css selector ".center" for "$type" in $train""")
                else -> JourneyResponseParseError.Unknown(
                    message = """Unknown error for css selector ".center" for "$type" in $train""",
                    cause = throwable,
                )
            }
        }
        .flatMap { centerElements ->
            centerElements.asSequence()
                .flatMap { center ->
                    val trainTime = center.getElementsByClass("train-time")
                    val trainTimeType = center.getElementsByClass("train-time-type")
                    if (type.equals(trainTimeType.text(), ignoreCase = true)) {
                        sequenceOf(trainTime.text())
                    } else {
                        emptySequence()
                    }
                }
                .firstOrNull()
                .rightIfNotNull {
                    JourneyResponseParseError.MalformedDocument("""Could not find time for type=$type in $train""")
                }
                .flatMap { trainTimeStr ->
                    Either.catch { LocalTime.parse(trainTimeStr, TIME_PATTERN) }
                        .mapLeft { throwable ->
                            when (throwable) {
                                is DateTimeParseException -> JourneyResponseParseError.IncorrectFormat(
                                    value = trainTimeStr,
                                    message = """Time="$trainTimeStr" does not match pattern="$TIME_PATTERN"""",
                                    cause = throwable,
                                )
                                else -> JourneyResponseParseError.Unknown(
                                    message = """Unknown error parsing time="$trainTimeStr" with pattern="$TIME_PATTERN"""",
                                    cause = throwable,
                                )
                            }
                        }
                }
        }

    private fun parseDistanceMeters(train: Element): Either<JourneyResponseParseError, Int> = Either
        .catch { train.requireSelect(".article-body") }
        .mapLeft { throwable ->
            when (throwable) {
                is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No elements for css selector ".article-body" for distance in $train""")
                else -> JourneyResponseParseError.Unknown(
                    """Unknown error for css selector ".article-body" for distance in $train""",
                    throwable
                )
            }
        }
        .flatMap { articleBodies ->
            articleBodies.getOrNull(0)
                .rightIfNotNull { JourneyResponseParseError.MalformedDocument("""Expected single match for ".article-body" for distance in $train""") }
        }
        .map { articleBody -> articleBody.children() }
        .flatMap { child ->
            child
                .asSequence()
                .map { it.text() }
                .mapNotNull { text -> """(\d+) km""".toRegex().matchEntire(text) }
                .flatMap { it.groupValues.drop(1) }
                .map { it.toDouble() }
                .firstOrNull()
                .rightIfNotNull {
                    JourneyResponseParseError.MalformedDocument("""Could not find matching distance for for $train""")
                }
        }
        .map { km -> (km * 1000).toInt() }
}