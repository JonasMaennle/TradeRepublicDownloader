package com.tr.websocket.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.tr.websocket.models.request.ConnectRequest
import com.tr.websocket.models.request.WebsocketRequest
import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.response.TimelineTransactionsResponse
import com.tr.websocket.models.response.WebsocketResponse
import jakarta.annotation.PostConstruct
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WebsocketService(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val websocketCallback: WebsocketCallback<WebsocketResponse>,
) : WebSocketListener() {
    @Value("\${tr.api.endpoint:}")
    private lateinit var apiEndpoint: String

    private lateinit var webSocket: WebSocket
    private var subCounter = 0

    @PostConstruct
    private fun connect() {
        subCounter = 1
        webSocket = OkHttpClient()
            .newWebSocket(Request.Builder().url(apiEndpoint).build(), this)
        logger.debug("Connecting to websocket")
        send("connect 31 ${objectMapper.writeValueAsString(ConnectRequest(LOCALE))}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        logger.trace("onMessage text has been called: $text")
        handleMessage(text)
    }

    private fun handleMessage(text: String) {
        if (text == "connected") {
            logger.debug("connected")
        } else {
            val subId = text.substring(0, 1)
            val type = text.substring(2, 3)
            if (type == "C") {
                unsubscribe(subId)
                return
            }

            val data = text.substring(4)
            for (targetClass in TARGET_CLASSES) {
                try {
                    val response = objectMapper.readValue(data, targetClass)
                    websocketCallback.onResponseReceived(response, this)
                    break
                } catch (_: Exception) {
                } finally {
                    logger.trace(data)
                }
            }
        }
    }

    private fun send(message: String) {
        webSocket.send(message)
        logger.trace("_send message: $message")
    }

    fun disconnect() {
        webSocket.close(1000, "Connection closed normally")
    }

    fun sub(load: String): Int {
        val subId = getNextSubId()
        send("sub $subId $load")
        return subId
    }

    private fun unsubscribe(id: String) {
        send("unsub $id")
    }

    private fun getNextSubId(): Int {
        return subCounter++
    }

    fun createNewRequest(request: WebsocketRequest) {
        sub(objectMapper.writeValueAsString(request))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebsocketService::class.java)
        private val TARGET_CLASSES =
            listOf(TimelineTransactionsResponse::class.java, TimelineDetailResponse::class.java)
        private var LOCALE = "de"
    }
}
