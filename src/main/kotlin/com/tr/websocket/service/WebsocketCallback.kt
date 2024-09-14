package com.tr.websocket.service

interface WebsocketCallback<T> {
    fun <T>onResponseReceived(response: T, websocketService: WebsocketService)
}