package com.tr.utils.models

enum class TimeFormatPattern(
    val patternString: String
) {
    FULL("dd.MM.yyyy"),
    PARTIAL("yyyy-MM")
}