package com.tr.utils

import java.lang.Exception

fun transformHeaderCookiesToMap(setCookies: List<String>?): Map<String, String> {
    if (setCookies.isNullOrEmpty()) throw Exception("Invalid Code. No session cookie received")
    val cookieMap = mutableMapOf<String, String>()

    for (cookieString in setCookies) {
        val cookieParts = cookieString.split(";").map { it.trim() }
        val cookieKeyValue = cookieParts[0].split("=")
        if (cookieKeyValue.size == 2) {
            val cookieName = cookieKeyValue[0]
            val cookieValue = cookieKeyValue[1]
            cookieMap[cookieName] = cookieValue
        }
    }
    return cookieMap
}