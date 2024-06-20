package com.tr.model.request

interface TRRequest

data class ConnectRequest(
    val locale: String
) : TRRequest

data class TimelineTransactionsRequest(
    val token: String,
    val after: String? = null,
    val type: String = "timelineTransactions"
) : TRRequest

data class TimelineDetailRequest(
    val token: String,
    val id: String,
    val type: String = "timelineDetailV2"
) : TRRequest

data class LoginRequest(
    val phoneNumber: String,
    val pin: String
)
