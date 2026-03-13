package com.downnotice.mobile.data.repository

import com.downnotice.mobile.data.model.FeedConfig
import com.downnotice.mobile.data.model.FeedEntry
import com.downnotice.mobile.data.model.FeedResult
import com.downnotice.mobile.data.model.IncidentStatus
import com.downnotice.mobile.data.network.FeedFetcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.temporal.ChronoUnit

class FeedRepository {

    private val fetcher = FeedFetcher()
    private var feedData: Map<String, FeedResult> = emptyMap()

    suspend fun fetchAllFeeds(
        feeds: List<FeedConfig>,
        historyHours: Int
    ): Map<String, FeedResult> = coroutineScope {
        val cutoff = Instant.now().minus(historyHours.toLong(), ChronoUnit.HOURS)

        val results = feeds.filter { it.enabled }.map { feed ->
            async {
                try {
                    val parsed = fetcher.fetch(feed.url)
                    val filtered = parsed.items.filter { item ->
                        try {
                            Instant.parse(item.pubDate).isAfter(cutoff)
                        } catch (_: Exception) {
                            true
                        }
                    }
                    FeedResult(
                        id = feed.id,
                        name = feed.name,
                        icon = feed.icon,
                        url = feed.url,
                        items = filtered,
                        status = computeFeedStatus(filtered),
                        lastFetch = Instant.now().toString(),
                        error = null
                    )
                } catch (e: Exception) {
                    FeedResult(
                        id = feed.id,
                        name = feed.name,
                        icon = feed.icon,
                        url = feed.url,
                        items = feedData[feed.id]?.items ?: emptyList(),
                        status = IncidentStatus.ERROR,
                        lastFetch = Instant.now().toString(),
                        error = e.message
                    )
                }
            }
        }.awaitAll()

        feedData = results.associateBy { it.id }
        feedData
    }

    fun getCachedFeeds(): Map<String, FeedResult> = feedData

    fun getOverallStatus(): IncidentStatus {
        val feeds = feedData.values
        if (feeds.isEmpty()) return IncidentStatus.OPERATIONAL

        if (feeds.any { it.status == IncidentStatus.ERROR }) return IncidentStatus.ERROR
        if (feeds.any { it.status == IncidentStatus.DOWN }) return IncidentStatus.DOWN
        if (feeds.any { it.status == IncidentStatus.DEGRADED }) return IncidentStatus.DEGRADED
        return IncidentStatus.OPERATIONAL
    }

    companion object {
        fun computeFeedStatus(items: List<FeedEntry>): IncidentStatus {
            val active = items.filter {
                it.status != IncidentStatus.RESOLVED && it.status != IncidentStatus.SCHEDULED
            }
            if (active.isEmpty()) return IncidentStatus.OPERATIONAL
            if (active.any { it.status == IncidentStatus.DOWN }) return IncidentStatus.DOWN
            if (active.any { it.status == IncidentStatus.DEGRADED }) return IncidentStatus.DEGRADED
            return IncidentStatus.OPERATIONAL
        }
    }
}
