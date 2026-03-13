package com.downnotice.mobile.data.model

data class FeedResult(
    val id: String,
    val name: String,
    val icon: String,
    val url: String,
    val items: List<FeedEntry>,
    val status: IncidentStatus,
    val lastFetch: String,
    val error: String? = null
)
