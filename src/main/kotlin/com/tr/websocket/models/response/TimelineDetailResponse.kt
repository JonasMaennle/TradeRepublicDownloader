package com.tr.websocket.models.response

data class TimelineDetailResponse(
    val id: String,
    val sections: List<Section>
) : WebsocketResponse

data class Section(
    val title: String,
    val data: Any?, // can be DocumentSection || OverviewSection
    val type: String
)
