package com.innercirclesoftware.wkd_core.services.impl

import arrow.core.*
import com.innercirclesoftware.wkd_api.models.Journey
import com.innercirclesoftware.wkd_api.models.JourneyOperationMode
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
    journeyOperationMode: JourneyOperationMode,
) -> SearchResponseJourneyBuilder = { start, end, distanceMeters, journeyOperationMode ->
    { searchTime, startStation, endStation ->
        Journey(
            start = JourneyStation(time = searchTime.withLocalTime(start), station = startStation),
            end = JourneyStation(time = searchTime.withLocalTime(end), station = endStation),
            distanceMeters = distanceMeters,
            journeyOperationMode = journeyOperationMode,
        )
    }
}

private val TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val JOURNEY_OPERATION_MODES_BY_ID = mapOf(
    "C" to JourneyOperationMode.WEEKENDS_AND_LIMITED_HOLIDAYS,
    "D" to JourneyOperationMode.WORKDAYS_EXCEPT_HOLIDAYS,
)

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
            .zip(
                parseEndTime(train),
                parseDistanceMeters(train),
                parseJourneyOperationMode(train)
            ) { start, end, distanceMeters, journeyOperationMode ->
                parsedSearchResponse(start, end, distanceMeters, journeyOperationMode)
            }
    }
    private val parseStartTime: (Element) -> Either<JourneyResponseParseError, LocalTime> =
        { parseTimeForType(it, "Odjazd") }
    private val parseEndTime: (Element) -> Either<JourneyResponseParseError, LocalTime> =
        { parseTimeForType(it, "Przyjazd") }
    private val parseJourneyOperationMode: (Element) -> Either<JourneyResponseParseError, JourneyOperationMode> =
        { train ->
            Either
                .catch { train.requireSelect(".train-details") }
                .mapLeft { throwable ->
                    when (throwable) {
                        is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No elements for css selector ".train-details" for journeyOperationMode in $train""")
                        else -> JourneyResponseParseError.Unknown(
                            message = """Unknown error for css selector ".train-details" for journeyOperationMode in $train""",
                            cause = throwable,
                        )
                    }
                }
                .flatMap { trainDetails ->
                    trainDetails.getOrNull(0)
                        .rightIfNotNull { JourneyResponseParseError.MalformedDocument("""Expected single match for ".train-details" for journeyOperationMode in $train""") }
                }
                .map { trainDetails ->
                    trainDetails
                        .children()
                        .asSequence()
                        .map { it.text() }
                        .mapNotNull { modeId -> JOURNEY_OPERATION_MODES_BY_ID[modeId] }
                        .firstOrNull() ?: JourneyOperationMode.ALWAYS
                }
        }

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
        .catch { train.requireSelect(".train-course-info") }
        .mapLeft { throwable ->
            when (throwable) {
                is NullPointerException -> JourneyResponseParseError.MalformedDocument("""No elements for css selector ".train-course-info" for distance in $train""")
                else -> JourneyResponseParseError.Unknown(
                    message = """Unknown error for css selector ".train-course-info" for distance in $train""",
                    cause = throwable,
                )
            }
        }
        .flatMap { trainCourseInfo ->
            trainCourseInfo.getOrNull(0)
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