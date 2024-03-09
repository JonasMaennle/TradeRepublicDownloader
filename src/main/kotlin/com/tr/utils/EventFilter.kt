package com.tr.utils

import com.tr.model.Document
import com.tr.model.TimelineDetailResponse
import com.tr.model.TimelineEvent

sealed class Filter
data object SavingPlanFilter : Filter()
data object DividendFilter : Filter()
data object InterestFilter : Filter()

class EventFilter(private val filter: Filter) {
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
            .filter { it.data.body.contains(identifier) && isInCurrentMonth(it.data.month, Pattern.PARTIAL) }
            .map { it.data.id }
    }
}