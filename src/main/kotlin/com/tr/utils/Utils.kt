package com.tr.utils

import com.tr.model.response.CustomHeaders
import io.github.oshai.kotlinlogging.KLogger
import okhttp3.Headers
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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

fun isInCurrentMonth(dateString: String?, pattern: Pattern = Pattern.FULL): Boolean {
    if (dateString.isNullOrEmpty()) return false
    val formatter = DateTimeFormatter.ofPattern(pattern.patternString)

    return when (pattern) {
        Pattern.FULL -> {
            val date = LocalDate.parse(dateString, formatter)
            val currentMonth = LocalDate.now().month
            val entryMonth = date.month
            currentMonth == entryMonth
        }
        Pattern.PARTIAL -> {
            val parsedDate = YearMonth.parse(dateString, formatter)
            val currentDate = YearMonth.now()
            parsedDate == currentDate
        }
    }
}

enum class Pattern(val patternString: String) {
    FULL("dd.MM.yyyy"),
    PARTIAL("yyyy-MM")
}