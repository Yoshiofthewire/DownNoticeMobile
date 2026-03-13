package com.downnotice.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.downnotice.mobile.data.model.IncidentStatus

@Composable
fun StatusBadge(status: IncidentStatus, modifier: Modifier = Modifier) {
    val bg = statusColor(status).copy(alpha = 0.15f)
    val fg = statusColor(status)
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = statusLabel(status),
            color = fg,
            fontSize = 12.sp
        )
    }
}
