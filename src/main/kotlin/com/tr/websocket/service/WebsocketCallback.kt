package com.tr.websocket.service

import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.response.TimelineTransactionsResponse

interface WebsocketCallback<T> {
    fun onTimelineEntryReceived(response: TimelineDetailResponse, websocketService: WebsocketService)
    fun onTimelineReceived(response: TimelineTransactionsResponse, websocketService: WebsocketService)
}