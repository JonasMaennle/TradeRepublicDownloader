package com.tr.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.tr.model.request.ConnectRequest
import com.tr.model.response.TimelineDetailV2Response
import com.tr.model.response.TimelineTransactionsResponse
import com.tr.service.TradeRepublicDownloadService
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketService(
    private val callback: TradeRepublicDownloadService,
    private val objectMapper: ObjectMapper
) : WebSocketListener() {
    companion object {
        private val TARGET_CLASSES = listOf(TimelineTransactionsResponse::class.java, TimelineDetailV2Response::class.java)
        private var LOCALE = "de"
        private var API_ENDPOINT = "wss://api.traderepublic.com"
    }

    private var ws: WebSocket
    private var subCounter = 1
    private val logger = KotlinLogging.logger {}

    init {
        val request = Request.Builder().url(API_ENDPOINT).build()
        ws = OkHttpClient().newWebSocket(request, this)
        logger.trace { "Connecting to Socket" }
        send("connect 31 ${objectMapper.writeValueAsString(ConnectRequest(LOCALE))}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        logger.debug { "onMessage text has been called: $text" }
        handleMessage(text)
    }

    private fun handleMessage(text: String) {
        if (text == "connected") {
            logger.trace { "connected" }
        } else {
            val subId = text.substring(0,1)
            val type = text.substring(2,3)
            if (type == "C") {
                unsubscribe(subId)
                return
            }

            val data = text.substring(4)
            for (targetClass in TARGET_CLASSES) {
                try {
                    val response = objectMapper.readValue(data, targetClass)
                    callback.onResponseReceived(response)
                    break
                } catch (_: Exception) {
                } finally {
                    logger.debug { data }
                }
            }
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
        ws.send(msg)
        logger.debug { "_send message: $msg" }
    }

    private fun getNextSubId(): Int {
        return subCounter++
    }

    fun disconnect() {
        ws.close(1000, "Connection closed normally")
    }
}