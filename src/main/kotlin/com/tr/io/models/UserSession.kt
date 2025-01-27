package com.tr.io.models

import com.tr.config.DownloadOption
import java.io.Serializable
import java.time.YearMonth

data class UserSession(
    val downloadOption: DownloadOption,
    val yearMonth: YearMonth,
    val sessionToken: String,
) : Serializable
