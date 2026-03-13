package com.downnotice.mobile.ui.components

import java.time.Instant
import java.time.temporal.ChronoUnit

fun formatTime(isoString: String): String {
    return try {
        val date = Instant.parse(isoString)
        val now = Instant.now()
        val diffMin = ChronoUnit.MINUTES.between(date, now)
        val diffHr = ChronoUnit.HOURS.between(date, now)

        when {
            diffMin < 0 -> "Scheduled"
            diffMin < 1 -> "Just now"
            diffMin < 60 -> "${diffMin}m ago"
            diffHr < 24 -> "${diffHr}h ago"
            else -> {
                val dt = java.time.ZonedDateTime.ofInstant(date, java.time.ZoneId.systemDefault())
                "${dt.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${dt.dayOfMonth}, ${dt.hour.toString().padStart(2, '0')}:${dt.minute.toString().padStart(2, '0')}"
            }
        }
    } catch (_: Exception) {
        isoString
    }
}
