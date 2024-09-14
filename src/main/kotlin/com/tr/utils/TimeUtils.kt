package com.tr.utils

import com.tr.utils.models.TimeFormatPattern
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun isMonthValid(dateString: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern(TimeFormatPattern.PARTIAL.patternString)
    return try {
        YearMonth.parse(dateString, formatter)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}

fun getCurrentMonth(pattern: TimeFormatPattern): String {
    return when (pattern) {
        TimeFormatPattern.FULL -> {
            LocalDate.now().month.toString()
        }
        TimeFormatPattern.PARTIAL -> {
            YearMonth.now().toString()
        }
    }
}

fun String.toYearMonth(): YearMonth =
    YearMonth.parse(this, DateTimeFormatter.ofPattern(TimeFormatPattern.PARTIAL.patternString))

fun isMonthEqual(dateString: String?, selectedMonth: YearMonth): Boolean {
    if (dateString.isNullOrEmpty()) return false
    return try {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val receivedDate = ZonedDateTime.parse(dateString, timestampFormatter)
        val receivedYearMonth = YearMonth.of(receivedDate.year, receivedDate.monthValue)
        return selectedMonth == receivedYearMonth
    } catch (e: DateTimeParseException) {
        false
    }
}

fun transformDate(dateString: String): String {
    val originalFormatter = DateTimeFormatter.ofPattern(TimeFormatPattern.FULL.patternString)
    val date = LocalDate.parse(dateString, originalFormatter)

    val desiredFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return date.format(desiredFormatter).toString()
}
