package com.tr.model.response

interface ExpectedResponse

data class TimelineResponse(
    val data: List<TimelineEvent>,
    val cursors: Cursors
) : ExpectedResponse

data class TimelineDetailResponse(
    val id: String,
    val titleText: String,
    val subtitleText: String,
    val sections: List<TimelineDetail>
) : ExpectedResponse

data class TimelineEvent(
    val type: String,
    val data: TimelineEventData
)

data class TimelineEventData(
    val id: String,
    val title: String,
    val body: String?,
    val action: Action?, // not available for "Einzahlung"
    val month: String?
)

data class TimelineDetail(
    val type: String,
    val title: String,
    val documents: List<Document>?
)

data class Document(
    val title: String,
    val detail: String,
    val action: Action,
    val id: String
)

data class Action(
    val type: String,
    val payload: Any?
)

data class Cursors(
    val before: String?,
    val after: String?
)

data class LoginResponse(
    val processId: String,
    val countdownInSeconds: Int
)