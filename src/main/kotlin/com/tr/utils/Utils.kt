package com.tr.utils

import com.tr.model.CustomHeaders
import okhttp3.Headers
import java.time.LocalDate
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

fun isInCurrentMonth(dateString: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val date = LocalDate.parse(dateString, formatter)

    val currentMonth = LocalDate.now().month
    val entryMonth = date.month

    return currentMonth == entryMonth
}