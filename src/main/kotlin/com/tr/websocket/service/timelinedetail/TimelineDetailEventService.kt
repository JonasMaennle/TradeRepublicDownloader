package com.tr.websocket.service.timelinedetail

import com.tr.io.models.UserSession
import com.tr.websocket.models.TimelineDetails
import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.TimelineEventType
import java.util.*

interface TimelineDetailEventService {
    fun parseEvent(event: TimelineDetailResponse, userSession: UserSession): TimelineDetails

    fun type(): TimelineEventType

    fun buildFileName(userSession: UserSession, date: String?, name: String?): Optional<String> {
        if (date.isNullOrEmpty()) {
            return Optional.empty();
        }
        val filenameTemplate = userSession.downloadOption.filename
        return Optional.of(
            filenameTemplate
                .replace("\$DATE", date.split("T")[0])
                .replace("\$NAME", name ?: "")
        )
    }
}