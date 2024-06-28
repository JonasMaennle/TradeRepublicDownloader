package com.tr.utils

import com.tr.model.Pattern
import com.tr.model.response.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

sealed class Filter
data object SavingPlanFilter : Filter()
data object DividendFilter : Filter()
data object InterestFilter : Filter()
data object OrderFilter : Filter()

class EventFilter(private val filter: Filter, private val selectedMonth: YearMonth) {
    fun fileNameBuilder(date: String, name: String?): String {
        return when (filter) {
            is SavingPlanFilter -> "Abrechnung Sparplan_${transformDate(date)}_${name}"
            is DividendFilter -> "Dividende_${transformDate(date)}_${name}"
            is InterestFilter -> "Zinsen_${transformDate(date)}"
            is OrderFilter -> "Kauf_${transformDate(date)}_${name}"
        }
    }

    fun applyTimelineEventFilter(items: List<TimelineTransactionsDetail>): List<String> {
        return when (filter) {
            is SavingPlanFilter -> applyFilter(items, listOf("SAVINGS_PLAN_EXECUTED"))
            is DividendFilter -> applyFilter(items, listOf("ssp_corporate_action_invoice_cash", "CREDIT"))
            is InterestFilter -> applyFilter(items, listOf("INTEREST_PAYOUT_CREATED"))
            is OrderFilter -> applyFilter(items, listOf("ORDER_EXECUTED", "TRADE_INVOICE"))
        }
    }

    private fun applyFilter(items: List<TimelineTransactionsDetail>, identifier: List<String>): List<String> {
        return items
            .filter { identifier.contains(it.eventType) && isMonthEqual(it.timestamp, selectedMonth) }
            .map { it.id }
    }

    private fun isMonthEqual(dateString: String?, selectedMonth: YearMonth): Boolean {
        if (dateString.isNullOrEmpty()) return false
        return try {
            val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            val receivedDate = ZonedDateTime.parse(dateString, timestampFormatter)
            val receivedYearMonth = YearMonth.of(receivedDate.year, receivedDate.monthValue)
            return selectedMonth == receivedYearMonth
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isInSelectedMonth(dateString: String?, pattern: Pattern = Pattern.FULL): Boolean {
        if (dateString.isNullOrEmpty()) return false
        return try {
            when (pattern) {
                Pattern.FULL -> {
                    val parsedDateA =
                        LocalDate.parse(dateString, DateTimeFormatter.ofPattern(Pattern.FULL.patternString))
                    parsedDateA.year == selectedMonth.year && parsedDateA.month == selectedMonth.month
                }

                Pattern.PARTIAL -> {
                    val parsedDateA =
                        YearMonth.parse(dateString, DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString))
                    parsedDateA == selectedMonth
                }
            }
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isTimelineInDateRange(timelineTransactionsResponse: TimelineTransactionsResponse): Boolean {
        val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        try {
            val lastTimestamp = timelineTransactionsResponse.items.last().timestamp
            val receivedDate = ZonedDateTime.parse(lastTimestamp, timestampFormatter)
            val receivedYearMonth = YearMonth.of(receivedDate.year, receivedDate.monthValue)
            return if (selectedMonth.isBefore(receivedYearMonth)) {
                true
            } else !selectedMonth.isAfter(receivedYearMonth)
        } catch (_: Exception) {
        }
        return false
    }
}
