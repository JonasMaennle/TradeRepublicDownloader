package com.tr.utils

import com.tr.model.Pattern
import com.tr.model.response.Document
import com.tr.model.response.TimelineDetailResponse
import com.tr.model.response.TimelineEvent
import com.tr.model.response.TimelineResponse
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

sealed class Filter
data object SavingPlanFilter : Filter()
data object DividendFilter : Filter()
data object InterestFilter : Filter()

class EventFilter(private val filter: Filter, private val selectedMonth: YearMonth) {
    fun fileNameBuilder(document: Document, timelineDetailResponse: TimelineDetailResponse): String {
        return when (filter) {
            is SavingPlanFilter -> {
                "${document.title}_${timelineDetailResponse.titleText}_${document.detail}"
            }
            is DividendFilter -> {
                val prefixTitle = timelineDetailResponse.sections.find { it.type == "text" }?.title
                return "${prefixTitle}_${timelineDetailResponse.titleText}_${document.detail}"
            }
            is InterestFilter -> {
                "Zinsen_${document.detail}"
            }
        }
    }

    fun applyTimelineEventFilter(data: List<TimelineEvent>): List<String> {
        return when (filter) {
            is SavingPlanFilter -> {
                applyFilter(data, "Sparplan")
            }
            is DividendFilter -> {
                applyFilter(data, "Dividende")
            }
            is InterestFilter -> {
                applyFilter(data, "Zinsen")
            }
        }
    }

    private fun applyFilter(data: List<TimelineEvent>, identifier: String): List<String> {
        return data
            .filter { it.data.body?.contains(identifier) ?: false && isInSelectedMonth(it.data.month, Pattern.PARTIAL) }
            .map { it.data.id }
    }

    fun isInSelectedMonth(dateString: String?, pattern: Pattern = Pattern.FULL): Boolean {
        if (dateString.isNullOrEmpty()) return false
        return try {
            when (pattern) {
                Pattern.FULL -> {
                    val parsedDateA = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(Pattern.FULL.patternString))
                    parsedDateA.year == selectedMonth.year && parsedDateA.month == selectedMonth.month
                }

                Pattern.PARTIAL -> {
                    val parsedDateA = YearMonth.parse(dateString, DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString))
                    parsedDateA == selectedMonth
                }
            }
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isTimelineInDateRange(timelineResponse: TimelineResponse): Boolean {
        val formatter = DateTimeFormatter.ofPattern(Pattern.PARTIAL.patternString)
        try {
            val lastEntry = timelineResponse.data.last()
            val entryMonth = YearMonth.parse(lastEntry.data.month, formatter)
            return if (selectedMonth.isBefore(entryMonth)) {
                true
            } else !selectedMonth.isAfter(entryMonth)
        } catch (_: Exception) {}
        return false
    }
}