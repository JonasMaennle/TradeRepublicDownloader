package com.tr.model

data class ConnectPayload(
    val locale: String
)

data class CustomHeaders(
    val date: String?,
    val contentLength: Int?,
    val vary: List<String>,
    val setCookies: List<String>
)

data class LoginData(
    val phoneNumber: String,
    val pin: String
)

data class LoginResponse(
    val processId: String,
    val countdownInSeconds: Int
)

data class TimeLineResponse(
    val data: List<TimelineEvent>,
    val cursors: Cursors
)

data class Cursors(
    val before: String,
    val after: String
)

data class TimelineEvent(
    val type: String,
    val data: TimelineEventData
)

data class TimelineEventData(
    val id: String?,
    val timestamp: Long?,
    val icon: String?,
    val title: String?,
    val body: String?,
    val cashChangeAmount: Double?,
    val action: Action?,
    val attributes: List<Any>?,
    val month: String?
)

data class Action(
    val type: String,
    val payload: String
)

data class TimelineDetailResponse(
    val id: String,
    val titleText: String,
    val subtitleText: String,
    val sections: List<TimelineDetail>
)

data class TimelineDetail(
    val type: String,
    val documents: List<Document>?
)

data class Document(
    val title: String,
    val detail: String,
    val action: Action,
    val id: String
)