package com.tr.websocket.service.timelinedetail

import com.tr.io.models.UserSession
import com.tr.websocket.models.TimelineDetails
import com.tr.websocket.models.response.TimelineDetailResponse
import com.tr.websocket.models.TimelineEventType
import org.springframework.stereotype.Service

@Service
class TimelineEventDispatcher(
    handlers: List<TimelineDetailEventService>
) {
    private val handlersByType =
        handlers.associateBy { it.type() }

    fun dispatch(event: TimelineDetailResponse, userSession: UserSession): TimelineDetails {

        val handler = handlersByType[getType(userSession)]
            ?: error("No handler for ${TimelineEventType.INTEREST}")

        return handler.parseEvent(event, userSession)
    }

    private fun getType(userSession: UserSession): TimelineEventType {
        return when (userSession.downloadOption.id) {
            "S" -> TimelineEventType.SAVINGS_PLAN
            "D" -> TimelineEventType.DIVIDEND
            "Z" -> TimelineEventType.INTEREST
            "O" -> TimelineEventType.TRADE
            else -> throw IllegalStateException("Invalid state in TimelineEventDispatcher")
        }
    }
}