package com.tr.websocket.models.response

data class TimelineTransactionsResponse(
    val items: List<TimelineTransactionsDetail>,
    val cursors: Cursors,
) : WebsocketResponse

data class TimelineTransactionsDetail(
    val id: String,
    val timestamp: String,
    val eventType: String,
)

data class Cursors(
    val before: String?,
    val after: String?,
)
