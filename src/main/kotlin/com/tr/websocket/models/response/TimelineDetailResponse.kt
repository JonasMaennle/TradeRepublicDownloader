package com.tr.websocket.models.response

import com.fasterxml.jackson.databind.JsonNode

data class TimelineDetailResponse(
    val id: String,
    val sections: List<Section> = emptyList()
) : WebsocketResponse

data class Section(
    val title: String,
    val data: JsonNode?,
    val type: String
)
