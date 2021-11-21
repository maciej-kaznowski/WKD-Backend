package com.innercirclesoftware.wkd_core.models

enum class JourneyOperation {
    /**
     * Trains operating Monday - Friday
     *
     * Exceptions: holidays, 24-12-2020
     */
    WORKDAYS_EXCEPT_HOLIDAYS,

    /**
     * Operates on Saturday and Sunday and some holidays
     *
     * Holidays included: 24-12-2020
     * Holidays excluded: 30-01-2021, 31-01-2021, 06-02-2021, 07-02-2021, 13-02-2021, 14-02-2021, 20-02-2021, 21-02-2021
     */
    WEEKENDS_AND_LIMITED_HOLIDAYS,
}