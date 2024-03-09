package com.tr.websocket

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tr.model.ConnectPayload
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketService<T>(private val clazz: Class<T>, private val callback: WebSocketCallback<T>) : WebSocketListener() {
    private var ws: WebSocket? = null
    private var subCounter = 1
    private var locale = "de"
    private var apiEndpoint = "wss://api.traderepublic.com"
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    private val logger = KotlinLogging.logger {}

    init {
        connect()
    }

    private fun connect() {
        val request = Request.Builder().url(apiEndpoint).build()
        ws = OkHttpClient().newWebSocket(request, this)
        logger.debug { "Connected to Socket" }
        openChannel()
    }

    private fun openChannel() {
        val payload = ConnectPayload(locale)
        val jsonPayload = objectMapper.writeValueAsString(payload)
        send("connect 26 $jsonPayload")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        logger.trace { "onMessage text has been called: $text" }
        handleMessage(text)
    }

    private fun handleMessage(text: String) {
        if (text == "connected") {
            logger.trace { "connected" }
        } else {
            val data = text.substring(4)
            val subId = text.substring(0,1)
            val type = text.substring(2,3)
            if (type == "C") {
                unsubscribe(subId)
                ws?.cancel()
            }
            try {
                callback.onResponseReceived(objectMapper.readValue(data, clazz))
            } catch (_: Exception) { }
        }
    }

    fun sub(load: String): Int {
        val subId = getNextSubId()
        send("sub $subId $load")
        return subId
    }

    private fun unsubscribe(id: String) {
        send("unsub $id")
    }

    private fun send(msg: String) {
        ws?.send(msg)
        logger.trace { "_send message: $msg" }
    }

    private fun getNextSubId(): Int {
        return subCounter++
    }
}