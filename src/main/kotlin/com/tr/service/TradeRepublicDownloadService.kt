package com.tr.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tr.model.Document
import com.tr.model.TimeLineResponse
import com.tr.model.TimelineDetailResponse
import com.tr.utils.EventFilter
import com.tr.utils.isInCurrentMonth
import com.tr.websocket.WebSocketCallback
import com.tr.websocket.WebSocketService
import io.github.oshai.kotlinlogging.KotlinLogging

class TradeRepublicDownloadService<T>(private val sessionToken: String, private val eventFilter: EventFilter) : WebSocketCallback<T> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    private val fileService = FileService()
    private val logger = KotlinLogging.logger {}

    fun fetchTimeline(clazz: Class<T>) {
        val webSocketService = WebSocketService(clazz, this)
        val load = objectMapper.writeValueAsString(mapOf("type" to "timeline", "token" to sessionToken))
        webSocketService.sub(load)
    }

    private fun fetchTimelineDetail(clazz: Class<T>, id: String) {
        val webSocketService = WebSocketService(clazz, this)
        val load = objectMapper.writeValueAsString(mapOf("type" to "timelineDetail", "token" to sessionToken, "id" to id))
        webSocketService.sub(load)
    }

    override fun <T>onResponseReceived(response: T) {
        when (response) {
            is TimeLineResponse -> {
                logger.debug { "Received timeline event" }
                // filter for events with selected document type
                val filteredTimelineEventIds = eventFilter.applyTimelineEventFilter(response.data)
                val timelineDetailInstance = TradeRepublicDownloadService<TimelineDetailResponse>(sessionToken, eventFilter)
                filteredTimelineEventIds.forEach { timelineDetailInstance.fetchTimelineDetail(TimelineDetailResponse::class.java, it) }
            }
            is TimelineDetailResponse -> {
                logger.debug { "Received timeline detail event" }
                val document: Document = response.sections
                    .filter { it.documents != null }
                    .flatMap { it.documents!! }
                    .find { isInCurrentMonth(it.detail) } ?: throw IllegalStateException("No document for current moth received")

                fileService.downloadFile(
                    document.action.payload,
                    eventFilter.fileNameBuilder(document, response)
                )
            }
        }
    }
}