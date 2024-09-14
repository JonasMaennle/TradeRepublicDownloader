package com.tr.websocket.models.request

data class TimelineDetailRequest(
    val token: String,
    val id: String,
    val type: String = "timelineDetailV2"
) : WebsocketRequest
