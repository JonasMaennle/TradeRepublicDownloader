package com.tr.websocket.service.timelinedetail

import com.tr.io.models.UserSession
import com.tr.websocket.models.TimelineDetails
import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.TimelineEventType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InterestTimelineDetailEventService: TimelineDetailEventService {

    override fun parseEvent(event: TimelineDetailResponse, userSession: UserSession, date: String?): TimelineDetails {
        val documentsSection = event.sections.find {
            it.title == "Dokumente"
        }

        val downloadUrl =
            documentsSection
                ?.data
                ?.get(0)
                ?.get("action")
                ?.get("payload")
                ?.get("path")
                ?.asText()

        if (downloadUrl.isNullOrEmpty()) {
            log.error("Download-URL is null")
        }

        if (date.isNullOrEmpty()) {
            log.error("Date is null")
        }

        return TimelineDetails(
            downloadUrl = downloadUrl,
            fileName = buildFileName(userSession, date, null)
        )
    }

    override fun type(): TimelineEventType {
        return TimelineEventType.INTEREST
    }

    companion object {
        private val log = LoggerFactory.getLogger(InterestTimelineDetailEventService::class.java)
    }
}