package com.tr.model

enum class Pattern(val patternString: String) {
    FULL("dd.MM.yyyy"),
    PARTIAL("yyyy-MM")
}

data class DownloadProgress(
    val current: Int,
    val total: Int
)

data class CustomHeaders(
    val date: String?,
    val contentLength: Int?,
    val vary: List<String>,
    val setCookies: List<String>
)