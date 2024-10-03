package com.tr.websocket.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tr.io.models.DownloadProgress
import com.tr.io.service.FileService
import com.tr.io.models.UserInput
import com.tr.io.service.UserInputService
import com.tr.utils.extractDateFromUrl
import com.tr.websocket.models.request.TimelineDetailRequest
import com.tr.websocket.models.request.TimelineTransactionsRequest
import com.tr.websocket.models.response.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class WebsocketResponseHandler(
    @Autowired private val filterService: FilterService,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val fileService: FileService,
    @Autowired @Lazy private val userInputService: UserInputService,
) : WebsocketCallback<WebsocketResponse> {

    private val filteredTimelineEventIds: MutableList<String> = mutableListOf()
    private var timelineReceivedCounter = 0
    private var documentsExpected = 0
    private var documentsReceived = 0

    override fun <T> onResponseReceived(response: T, websocketService: WebsocketService) {
        val userInput: UserInput = userInputService.userInput
        when (response) {
            is TimelineTransactionsResponse -> {
                logger.debug("Received TimelineTransactionsResponse event")
                filteredTimelineEventIds.addAll(
                    filterService.filterForTimelineIds(
                        response.items,
                        userInput
                    )
                )

                // fetch older timeline entries -> one request contains 30 entries
                if (!response.cursors.after.isNullOrEmpty() && filterService.isTimelineInDateRange(
                        response,
                        userInput
                    )
                ) {
                    timelineReceivedCounter++
                    websocketService.createNewRequest(TimelineTransactionsRequest(userInput.sessionToken, after = response.cursors.after))
                    return
                }

                documentsExpected = filteredTimelineEventIds.size
                if (documentsExpected == 0) {
                    logger.warn("No matching documents found in your timeline")
                    terminateApplication(userInput.sessionToken)
                }

                // request detail pages
                filteredTimelineEventIds.forEach {
                    websocketService.createNewRequest(
                        TimelineDetailRequest(
                            userInput.sessionToken,
                            it
                        )
                    )
                }
            }

            is TimelineDetailResponse -> {
                logger.debug("Received TimelineDetailResponse event")
                // find matching section
                val documentSection: Section? = response.sections.find { it.type == "documents" }
                val overviewSection: Section? = response.sections.find { it.title == "Übersicht" }
                if (documentSection == null || overviewSection == null) return

                // find pdf download url
                val documentString = objectMapper.writeValueAsString(documentSection.data)
                val document: DocumentSection? = objectMapper.readValue<List<DocumentSection>>(documentString).firstOrNull()

                // find company name
                val overviewString = objectMapper.writeValueAsString(overviewSection.data)
                val overviewList: List<OverviewSection> = objectMapper.readValue(overviewString)
                val name =
                    overviewList.find { it.title == "Asset" || it.title == "Wertpapier" }?.detail?.text ?: "missing"

                documentsReceived++
                if (document == null) {
                    println()
                    logger.warn("No downloadable PDF found for ${response.id}")
                } else {
                    val date = extractDateFromUrl(document.action.payload as String) ?: "invalid date"
                    fileService.downloadFile(
                        document.action.payload,
                        fileService.buildFileName(userInput, date, name),
                        DownloadProgress(documentsReceived, documentsExpected)
                    )
                }
                if (documentsReceived == documentsExpected) {
                    fileService.openFolder()
                    terminateApplication(userInput.sessionToken)
                }
            }
        }
    }

    private fun terminateApplication(sessionToken: String) {
        reset()
        userInputService.checkForNextRequest(sessionToken)
    }

    fun reset() {
        filteredTimelineEventIds.clear()
        timelineReceivedCounter = 0
        documentsExpected = 0
        documentsReceived = 0
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebsocketResponseHandler::class.java)
    }
}
