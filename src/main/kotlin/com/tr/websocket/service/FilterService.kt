package com.tr.websocket.service

import com.tr.io.models.DownloadOptions
import com.tr.io.models.UserInput
import com.tr.utils.isMonthEqual
import com.tr.websocket.models.response.TimelineTransactionsDetail
import com.tr.websocket.models.response.TimelineTransactionsResponse
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class FilterService {

    fun filterForTimelineIds(entries: List<TimelineTransactionsDetail>, userInput: UserInput) =
        when (userInput.documentType) {
            DownloadOptions.DIVIDEND -> applyTimelineFilter(
                entries,
                listOf("ssp_corporate_action_invoice_cash", "CREDIT"),
                userInput.yearMonth
            )

            DownloadOptions.SAVINGS_PLAN -> applyTimelineFilter(
                entries,
                listOf("SAVINGS_PLAN_EXECUTED", "SAVINGS_PLAN_INVOICE_CREATED"),
                userInput.yearMonth
            )

            DownloadOptions.INTEREST -> applyTimelineFilter(
                entries,
                listOf("INTEREST_PAYOUT_CREATED", "INTEREST_PAYOUT"),
                userInput.yearMonth
            )

            DownloadOptions.ORDER -> applyTimelineFilter(
                entries,
                listOf("ORDER_EXECUTED", "TRADE_INVOICE"),
                userInput.yearMonth
            )
        }

    private fun applyTimelineFilter(
        entries: List<TimelineTransactionsDetail>,
        identifier: List<String>,
        yearMonth: YearMonth
    ) =
        entries
            .filter { identifier.contains(it.eventType) && isMonthEqual(it.timestamp, yearMonth) }
            .map { it.id }

    fun isTimelineInDateRange(timelineTransactionsResponse: TimelineTransactionsResponse, userInput: UserInput): Boolean {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        try {
            val lastTimestamp = timelineTransactionsResponse.items.last().timestamp
            val receivedDate = ZonedDateTime.parse(lastTimestamp, timestampFormatter)
            val receivedYearMonth = YearMonth.of(receivedDate.year, receivedDate.monthValue)
            return if (userInput.yearMonth.isBefore(receivedYearMonth)) {
                true
            } else !userInput.yearMonth.isAfter(receivedYearMonth)
        } catch (_: Exception) {
        }
        return false
    }
}
