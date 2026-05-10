package com.tr.websocket.service.timelinedetail

import com.tr.io.models.UserSession
import com.tr.websocket.models.TimelineDetails
import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.TimelineEventType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SavingsPlanTimelineDetailEventService : TimelineDetailEventService {

    override fun parseEvent(event: TimelineDetailResponse, userSession: UserSession): TimelineDetails {
        val documentsSection = event.sections.find {
            it.title == "Dokumente"
        }

        val headerSection = event.sections.find {
            it.type == "header"
        }

        val savingPlanSection = event.sections.find {
            it.title == "Sparplan"
        }

        val downloadUrl =
            documentsSection
                ?.data
                ?.get(0)
                ?.get("action")
                ?.get("payload")
                ?.asText()

        val date =
            headerSection
                ?.data
                ?.get("timestamp")
                ?.asText()

        val companyName =
            savingPlanSection
                ?.data
                ?.get(0)
                ?.get("detail")
                ?.get("title")
                ?.asText()

        if (downloadUrl.isNullOrEmpty()) {
            log.error("Download-URL is null")
        }

        if (date.isNullOrEmpty()) {
            log.error("Date is null")
        }

        if (companyName.isNullOrEmpty()) {
            log.error("CompanyName is null")
        }

        return TimelineDetails(
            downloadUrl = downloadUrl,
            fileName = buildFileName(userSession, date, companyName),
        )
    }

    override fun type(): TimelineEventType {
        return TimelineEventType.SAVINGS_PLAN
    }

    companion object {
        private val log = LoggerFactory.getLogger(SavingsPlanTimelineDetailEventService::class.java)
    }
}