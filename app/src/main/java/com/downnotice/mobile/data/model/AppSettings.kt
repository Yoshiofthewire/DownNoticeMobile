package com.downnotice.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val feeds: List<FeedConfig> = DEFAULT_FEEDS,
    val refreshInterval: Int = 15,
    val theme: String = "system",
    val historyHours: Int = 48,
    val notifications: Boolean = true
) {
    companion object {
        val DEFAULT_FEEDS = listOf(
            FeedConfig(
                id = "azure",
                name = "Microsoft Azure",
                url = "https://azure.status.microsoft/en-us/status/feed/",
                icon = "azure"
            ),
            FeedConfig(
                id = "aws",
                name = "Amazon Web Services",
                url = "https://status.aws.amazon.com/rss/all.rss",
                icon = "aws"
            ),
            FeedConfig(
                id = "gcp",
                name = "Google Cloud Platform",
                url = "https://status.cloud.google.com/en/feed.atom",
                icon = "gcp"
            ),
            FeedConfig(
                id = "github",
                name = "GitHub",
                url = "https://www.githubstatus.com/history.rss",
                icon = "github"
            ),
            FeedConfig(
                id = "cloudflare",
                name = "Cloudflare",
                url = "https://www.cloudflarestatus.com/history.atom",
                icon = "cloudflare"
            )
        )
    }
}
