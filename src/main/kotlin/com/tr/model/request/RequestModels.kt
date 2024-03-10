package com.tr.model.request

interface TRRequest

data class ConnectRequest(
    val locale: String
) : TRRequest

data class TimelineRequest(
    val token: String,
    val type: String = "timeline"
) : TRRequest

data class TimelineDetailRequest(
    val token: String,
    val id: String,
    val type: String = "timelineDetail"
) : TRRequest

data class LoginRequest(
    val phoneNumber: String,
    val pin: String
)