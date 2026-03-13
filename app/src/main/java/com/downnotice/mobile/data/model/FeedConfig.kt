package com.downnotice.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedConfig(
    val id: String,
    val name: String,
    val url: String,
    val icon: String = "generic",
    val enabled: Boolean = true
)
