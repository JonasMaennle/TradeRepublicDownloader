package com.tr.websocket.models.request

data class TimelineTransactionsRequest(
    val token: String,
    val after: String? = null,
    val type: String = "timelineTransactions"
) : WebsocketRequest
