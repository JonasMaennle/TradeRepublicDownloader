package com.tr.websocket

interface WebSocketCallback<T> {
    fun <T>onResponseReceived(response: T)
}