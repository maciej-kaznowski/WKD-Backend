package com.innercirclesoftware.wkd_core

import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object Wkd {

    val TIMEZONE: ZoneId = ZoneId.of("Europe/Warsaw")

    val TIME_PATTERN: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val DATE_PATTERN: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE


}