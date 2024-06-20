package com.tr.model.response

interface ExpectedResponse

data class TimelineTransactionsResponse(
    val items: List<TimelineTransactionsDetail>,
    val cursors: Cursors
) : ExpectedResponse

data class TimelineTransactionsDetail(
    val id: String,
    val timestamp: String,
    val title: String?,
    val subtitle: String?,
    val eventType: String
)

data class TimelineDetailV2Response(
    val id: String,
    val sections: List<Section>
) : ExpectedResponse

data class Section(
    val title: String,
    val data: Any?, // can be DocumentSection || OverviewSection
    val type: String
)

data class DocumentSection(
    val title: String,
    val detail: String,
    val action: Action,
    val id: String
)

data class Action(
    val type: String,
    val payload: Any?
)

data class OverviewSection(
    val title: String,
    val detail: OverviewSectionDetail
)

data class OverviewSectionDetail(
    val text: String
)

data class Cursors(
    val before: String?,
    val after: String?
)

data class LoginResponse(
    val processId: String,
    val countdownInSeconds: Int
)
