package com.tr.utils

import com.tr.model.response.CustomHeaders
import io.github.oshai.kotlinlogging.KLogger
import okhttp3.Headers
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun transformCookiesToMap(setCookies: List<String>): Map<String, String> {
    val cookiesMap = mutableMapOf<String, String>()
    for (cookieString in setCookies) {
        val cookieParts = cookieString.split(";").map { it.trim() }
        val cookieKeyValue = cookieParts[0].split("=")
        if (cookieKeyValue.size == 2) {
            val cookieName = cookieKeyValue[0]
            val cookieValue = cookieKeyValue[1]
            cookiesMap[cookieName] = cookieValue
        }
    }
    return cookiesMap
}

fun Headers.toCustomHeaders(): CustomHeaders {
    val date = get("date")
    val contentLength = get("content-length")?.toIntOrNull()
    val vary = values("vary")
    val setCookies = values("set-cookie")

    return CustomHeaders(date, contentLength, vary, setCookies)
}

fun getUserInput(prompt: String, logger: KLogger, validation: (String) -> Boolean): String {
    var response: String
    do {
        logger.info { prompt }
        response = readln()
    } while (!validation(response))
    return response
}

fun getCurrentMonth(pattern: Pattern): String {
    return when (pattern) {
        Pattern.FULL -> {
            LocalDate.now().month.toString()
        }
        Pattern.PARTIAL -> {
            YearMonth.now().toString()
        }
    }
}

fun isMonthValid(dateString: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString)
    return try {
        YearMonth.parse(dateString, formatter)
        true
    } catch (_: DateTimeParseException){
        false
    }
}

enum class Pattern(val patternString: String) {
    FULL("dd.MM.yyyy"),
    PARTIAL("yyyy-MM")
}