package com.tr.websocket.models.response

data class DocumentSection(
    val title: String,
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