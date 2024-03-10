package com.tr.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tr.model.request.TRRequest
import com.tr.model.request.TimelineDetailRequest
import com.tr.model.response.*
import com.tr.utils.EventFilter
import com.tr.utils.getUserInput
import com.tr.utils.isInCurrentMonth
import com.tr.websocket.WebSocketCallback
import com.tr.websocket.WebSocketService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class TradeRepublicDownloadService(private val sessionToken: String, private val eventFilter: EventFilter) : WebSocketCallback<ExpectedResponse> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    private val fileService = FileService()
    private val logger = KotlinLogging.logger {}
    private val webSocketService = WebSocketService(this, objectMapper)
    private var documentsExpected = 0
    private var documentsReceived = 0

    fun createNewSubRequest(tRRequest: TRRequest) {
        webSocketService.sub(objectMapper.writeValueAsString(tRRequest))
    }

    override fun <T>onResponseReceived(response: T) {
        when (response) {
            is TimelineResponse -> {
                logger.debug { "Received timeline event" }
                // filter for events with selected document type
                val filteredTimelineEventIds = eventFilter.applyTimelineEventFilter(response.data)
                documentsExpected = filteredTimelineEventIds.size
                filteredTimelineEventIds.forEach { this.createNewSubRequest(TimelineDetailRequest(sessionToken, it)) }
            }
            is TimelineDetailResponse -> {
                logger.debug { "Received timeline detail event" }
                val document: Document = response.sections
                    .filter { it.documents != null }
                    .flatMap { it.documents!! }
                    .find { isInCurrentMonth(it.detail) } ?: throw IllegalStateException("No document for current moth received")

                documentsReceived++
                fileService.downloadFile(
                    document.action.payload,
                    eventFilter.fileNameBuilder(document, response),
                    DownloadProgress(documentsReceived, documentsExpected)
                )

                if (documentsReceived == documentsExpected) terminateApplication()
            }
        }
    }

    private fun terminateApplication() {
        webSocketService.disconnect()
        println()
        logger.info { "Job finished successful. See you next time :)" }

        getUserInput("Enter 'q' to end the application:", logger) { it == "q" }
        exitProcess(0)
    }
}