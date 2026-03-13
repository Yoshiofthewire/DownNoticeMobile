package com.downnotice.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedEntry(
    val title: String,
    val description: String = "",
    val link: String = "",
    val pubDate: String = "",
    val status: IncidentStatus = IncidentStatus.UNKNOWN
)
