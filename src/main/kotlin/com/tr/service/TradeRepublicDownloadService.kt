package com.tr.service

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tr.model.DownloadProgress
import com.tr.model.request.TRRequest
import com.tr.model.request.TimelineDetailRequest
import com.tr.model.request.TimelineTransactionsRequest
import com.tr.model.response.*
import com.tr.utils.*
import com.tr.websocket.WebSocketCallback
import com.tr.websocket.WebSocketService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.system.exitProcess

class TradeRepublicDownloadService(private val sessionToken: String, private val eventFilter: EventFilter, private val login: Login) : WebSocketCallback<ExpectedResponse> {
    private val objectMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
    private val fileService = FileService()
    private val logger = KotlinLogging.logger {}
    private val webSocketService = WebSocketService(this, objectMapper)
    private var documentsExpected = 0
    private var documentsReceived = 0
    private var timelineReceivedCounter = 0
    private val filteredTimelineEventIds: MutableList<String> = mutableListOf()

    fun createNewSubRequest(tRRequest: TRRequest) {
        webSocketService.sub(objectMapper.writeValueAsString(tRRequest))
    }

    override fun <T>onResponseReceived(response: T) {
        when (response) {
            is TimelineTransactionsResponse -> {
                logger.debug { "Received timelineTransactions event" }
                filteredTimelineEventIds.addAll(eventFilter.applyTimelineEventFilter(response.items))

                // fetch older timeline entries -> one request contains 30 entries
                if (!response.cursors.after.isNullOrEmpty() && eventFilter.isTimelineInDateRange(response)) {
                    timelineReceivedCounter++
                    createNewSubRequest(TimelineTransactionsRequest(sessionToken, response.cursors.after))
                    return
                }

                documentsExpected = filteredTimelineEventIds.size
                if (documentsExpected == 0) {
                    logger.info { "No matching documents found in your timeline" }
                    terminateApplication()
                }
                filteredTimelineEventIds.forEach { this.createNewSubRequest(TimelineDetailRequest(sessionToken, it)) }
            }

            is TimelineDetailV2Response -> {
                logger.debug { "Received timeline detail event" }
                // find matching section
                val documentSection: Section? = response.sections.find { it.type == "documents" }
                val overviewSection: Section? = response.sections.find { it.title == "Übersicht" }
                if (documentSection == null || overviewSection == null) return

                // find pdf download url
                val documentString = objectMapper.writeValueAsString(documentSection.data)
                val documents: List<DocumentSection> = objectMapper.readValue(documentString)
                val documentData: DocumentSection? = documents.find { eventFilter.isInSelectedMonth(it.detail) }

                // find company name
                val overviewString = objectMapper.writeValueAsString(overviewSection.data)
                val overviewList: List<OverviewSection> = objectMapper.readValue(overviewString)
                val name = overviewList.find { it.title == "Asset" || it.title == "Wertpapier" }?.detail?.text ?: "missing"

                documentsReceived++
                if (documentData == null) {
                    println()
                    this.logger.info { "No downloadable PDF found for ${response.id}" }
                } else {
                    fileService.downloadFile(
                        documentData.action.payload as String,
                        eventFilter.fileNameBuilder(documentData.detail, name),
                        DownloadProgress(documentsReceived, documentsExpected)
                    )
                }
                if (documentsReceived == documentsExpected) terminateApplication()
            }
        }
    }

    private fun terminateApplication() {
        webSocketService.disconnect()
        println()
        logger.info { "Job finished successful :)" }

        val userInput = getUserInput("Enter 'X' to close the application or 'Q' to start another query:", logger) { it == "X" || it == "Q" }
        if (userInput == "X") exitProcess(0)

        login.processUserInput(sessionToken) // maybe refresh session token
    }
}
