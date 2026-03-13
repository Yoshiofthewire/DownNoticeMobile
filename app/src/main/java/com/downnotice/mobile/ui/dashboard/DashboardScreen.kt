package com.downnotice.mobile.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.downnotice.mobile.MainViewModel
import com.downnotice.mobile.data.model.FeedResult
import com.downnotice.mobile.data.model.IncidentStatus
import com.downnotice.mobile.ui.components.ProviderIcon
import com.downnotice.mobile.ui.components.StatusBadge
import com.downnotice.mobile.ui.components.StatusDot
import com.downnotice.mobile.ui.components.formatTime
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onFeedClick: (String) -> Unit
) {
    val feeds by viewModel.feeds.collectAsState()
    val overallStatus by viewModel.overallStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Feeds", "Notices")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(status = overallStatus, size = 12.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DownNotice")
                }
            },
            actions = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(12.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { viewModel.refreshFeeds() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        val feedList = feeds.values.toList()

        when (selectedTab) {
            0 -> FeedsTab(feedList = feedList, isLoading = isLoading, onFeedClick = onFeedClick)
            1 -> NoticesTab(feedList = feedList, isLoading = isLoading, onFeedClick = onFeedClick)
        }
    }
}

// ── Tab 0: Feed list ─────────────────────────────────────────────────────────

@Composable
private fun FeedsTab(
    feedList: List<FeedResult>,
    isLoading: Boolean,
    onFeedClick: (String) -> Unit
) {
    if (feedList.isEmpty() && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No feeds configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(feedList) { feed ->
                FeedRow(feed = feed, onClick = { onFeedClick(feed.id) })
            }
        }
    }
}

@Composable
private fun FeedRow(feed: FeedResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(status = feed.status, size = 16.dp)
            Spacer(modifier = Modifier.width(12.dp))
            ProviderIcon(icon = feed.icon, size = 24.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = feed.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            StatusBadge(status = feed.status)
        }
    }
}

// ── Tab 1: Notices (all current incidents) ───────────────────────────────────

@Composable
private fun NoticesTab(
    feedList: List<FeedResult>,
    isLoading: Boolean,
    onFeedClick: (String) -> Unit
) {
    val allItems = feedList.flatMap { feed ->
        feed.items.map { item ->
            TimelineItem(
                feedId = feed.id,
                feedName = feed.name,
                feedIcon = feed.icon,
                title = item.title,
                description = item.description,
                pubDate = item.pubDate,
                status = item.status
            )
        }
    }.sortedWith(
        compareBy<TimelineItem> { severityOrder(it.status) }
            .thenByDescending {
                try { Instant.parse(it.pubDate) } catch (_: Exception) { Instant.EPOCH }
            }
    )

    if (allItems.isEmpty() && !isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No incidents in the last 48 hours",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allItems) { item ->
                TimelineCard(item = item, onClick = { onFeedClick(item.feedId) })
            }
        }
    }
}

// ── Shared private components ─────────────────────────────────────────────────

@Composable
private fun TimelineCard(item: TimelineItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(status = item.status)
                Spacer(modifier = Modifier.width(6.dp))
                ProviderIcon(icon = item.feedIcon, size = 20.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            if (item.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description.take(200),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.feedName} • ${formatTime(item.pubDate)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class TimelineItem(
    val feedId: String,
    val feedName: String,
    val feedIcon: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val status: IncidentStatus
)

private fun severityOrder(status: IncidentStatus): Int = when (status) {
    IncidentStatus.DOWN -> 0
    IncidentStatus.ERROR -> 1
    IncidentStatus.DEGRADED -> 2
    IncidentStatus.UNKNOWN -> 3
    IncidentStatus.SCHEDULED -> 4
    IncidentStatus.RESOLVED -> 5
    IncidentStatus.OPERATIONAL -> 6
}
