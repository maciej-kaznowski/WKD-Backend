package com.innercirclesoftware.wkd_core

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object Wkd {

    val TIMEZONE: ZoneId = ZoneId.of("Europe/Warsaw")

    fun Instant.withLocalTime(time: LocalTime): Instant {
        return atZone(TIMEZONE).withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0).toInstant()
    }
}