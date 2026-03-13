package com.downnotice.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.downnotice.mobile.data.model.IncidentStatus
import com.downnotice.mobile.ui.theme.BlackStatus
import com.downnotice.mobile.ui.theme.Green500
import com.downnotice.mobile.ui.theme.Red500
import com.downnotice.mobile.ui.theme.Yellow500

@Composable
fun StatusDot(
    status: IncidentStatus,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(statusColor(status))
    )
}

fun statusColor(status: IncidentStatus): Color = when (status) {
    IncidentStatus.OPERATIONAL -> Green500
    IncidentStatus.RESOLVED -> Green500
    IncidentStatus.SCHEDULED -> Green500
    IncidentStatus.DEGRADED -> Yellow500
    IncidentStatus.DOWN -> Red500
    IncidentStatus.ERROR -> BlackStatus
    IncidentStatus.UNKNOWN -> Yellow500
}

fun statusLabel(status: IncidentStatus): String = when (status) {
    IncidentStatus.OPERATIONAL -> "Operational"
    IncidentStatus.DEGRADED -> "Degraded"
    IncidentStatus.DOWN -> "Down"
    IncidentStatus.ERROR -> "Error"
    IncidentStatus.RESOLVED -> "Resolved"
    IncidentStatus.SCHEDULED -> "Scheduled"
    IncidentStatus.UNKNOWN -> "Unknown"
}
