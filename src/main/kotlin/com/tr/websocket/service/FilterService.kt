package com.tr.websocket.service

import com.tr.io.models.UserSession
import com.tr.utils.isMonthEqual
import com.tr.websocket.models.response.TimelineTransactionsDetail
import com.tr.websocket.models.response.TimelineTransactionsResponse
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class FilterService {

    fun applyTimelineFilter(
        entries: List<TimelineTransactionsDetail>,
        userSession: UserSession,
    ) =
        entries
            .filter {
                userSession.downloadOption.identifier.contains(it.eventType) && isMonthEqual(
                    it.timestamp,
                    userSession.yearMonth
                )
            }
            .map { it.id }

    fun isTimelineInDateRange(
        timelineTransactionsResponse: TimelineTransactionsResponse,
        userSession: UserSession
    ): Boolean {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        try {
            val lastTimestamp = timelineTransactionsResponse.items.last().timestamp
            val receivedDate = ZonedDateTime.parse(lastTimestamp, timestampFormatter)
            val receivedYearMonth = YearMonth.of(receivedDate.year, receivedDate.monthValue)
            return if (userSession.yearMonth.isBefore(receivedYearMonth)) {
                true
            } else !userSession.yearMonth.isAfter(receivedYearMonth)
        } catch (_: Exception) {
        }
        return false
    }
}
