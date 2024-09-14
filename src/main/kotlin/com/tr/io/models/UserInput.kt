package com.tr.io.models

import java.io.Serializable
import java.time.YearMonth

data class UserInput(
    val documentType: DownloadOptions,
    val yearMonth: YearMonth,
    val sessionToken: String,
) : Serializable
