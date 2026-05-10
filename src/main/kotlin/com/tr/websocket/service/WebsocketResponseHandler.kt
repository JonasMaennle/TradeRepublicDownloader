package com.tr.websocket.service

import com.tr.io.models.DownloadProgress
import com.tr.io.service.FileService
import com.tr.io.models.UserSession
import com.tr.io.service.UserInputService
import com.tr.websocket.models.TimelineDetails
import com.tr.websocket.models.request.TimelineDetailRequest
import com.tr.websocket.models.request.TimelineTransactionsRequest
import com.tr.websocket.models.response.*
import com.tr.websocket.service.timelinedetail.TimelineEventDispatcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class WebsocketResponseHandler(
    @Autowired private val filterService: FilterService,
    @Autowired private val fileService: FileService,
    @Autowired @Lazy private val userInputService: UserInputService,
    @Autowired private val dispatcher: TimelineEventDispatcher,
) : WebsocketCallback<WebsocketResponse> {

    private val filteredTimelineEventIds: MutableList<String> = mutableListOf()
    private var timelineReceivedCounter = 0
    private var documentsExpected = 0
    private var documentsReceived = 0

    override fun onTimelineReceived(response: TimelineTransactionsResponse, websocketService: WebsocketService) {
        val userSession: UserSession = userInputService.userSession

        logger.debug("Received TimelineTransactionsResponse event")
        filteredTimelineEventIds.addAll(
            filterService.applyTimelineFilter(
                response.items,
                userSession
            )
        )

        // fetch older timeline entries -> one request contains 30 entries
        if (!response.cursors.after.isNullOrEmpty() && filterService.isTimelineInDateRange(
                response,
                userSession
            )
        ) {
            timelineReceivedCounter++
            websocketService.createNewRequest(
                TimelineTransactionsRequest(
                    userSession.sessionToken,
                    after = response.cursors.after
                )
            )
            return
        }

        documentsExpected = filteredTimelineEventIds.size
        if (documentsExpected == 0) {
            logger.warn("No matching documents found in your timeline")
            checkForAnotherQuery(userSession.sessionToken)
        }

        // request detail pages
        filteredTimelineEventIds.forEach {
            websocketService.createNewRequest(
                TimelineDetailRequest(
                    userSession.sessionToken,
                    it
                )
            )
        }
    }

    override fun onTimelineEntryReceived(response: TimelineDetailResponse, websocketService: WebsocketService) {
        val userSession: UserSession = userInputService.userSession

        val timelineDetails: TimelineDetails = dispatcher.dispatch(response, userSession)

        if (timelineDetails.fileName.isEmpty) {
            logger.warn("No downloadable file available for id ${response.id} yet. Please try again later.")
            logger.warn("Reduced expected documents by 1")
            documentsExpected--
            return
        }

        if (timelineDetails.downloadUrl.isNullOrEmpty()) {
            logger.warn("No downloadable file available for ${timelineDetails.fileName} yet. Please try again later.")
            logger.warn("Reduced expected documents by 1")
            documentsExpected--
            return
        }

        documentsReceived++
        fileService.downloadFile(
            timelineDetails.downloadUrl,
            timelineDetails.fileName.get(),
            DownloadProgress(documentsReceived, documentsExpected)
        )

        if (documentsReceived == documentsExpected) {
            fileService.openFolder()
            checkForAnotherQuery(userSession.sessionToken)
        }
    }

    private fun checkForAnotherQuery(sessionToken: String) {
        resetProperties()
        userInputService.checkForNextRequest(sessionToken)
    }

    private fun resetProperties() {
        filteredTimelineEventIds.clear()
        timelineReceivedCounter = 0
        documentsExpected = 0
        documentsReceived = 0
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebsocketResponseHandler::class.java)
    }
}
