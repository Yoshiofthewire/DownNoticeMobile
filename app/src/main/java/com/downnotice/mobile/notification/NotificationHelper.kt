package com.downnotice.mobile.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.downnotice.mobile.MainActivity
import com.downnotice.mobile.R
import com.downnotice.mobile.data.model.FeedEntry
import com.downnotice.mobile.data.model.FeedResult
import com.downnotice.mobile.data.model.IncidentStatus
import com.downnotice.mobile.data.model.StatusColor

class NotificationHelper(private val context: Context) {

    private val notifiedItems = mutableSetOf<String>()

    companion object {
        const val CHANNEL_STATUS = "downnotice_status"
        const val CHANNEL_INCIDENTS = "downnotice_incidents"
        const val STATUS_NOTIFICATION_ID = 1
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)

        val statusChannel = NotificationChannel(
            CHANNEL_STATUS,
            "Overall Status",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification showing overall cloud status"
        }

        val incidentChannel = NotificationChannel(
            CHANNEL_INCIDENTS,
            "Incident Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for new cloud outages and incidents"
        }

        manager.createNotificationChannel(statusChannel)
        manager.createNotificationChannel(incidentChannel)
    }

    fun updateStatusNotification(overallStatus: IncidentStatus) {
        if (!hasNotificationPermission()) return

        val (iconRes, title) = when (IncidentStatus.color(overallStatus)) {
            StatusColor.GREEN -> R.drawable.ic_notification_green to "All Systems Operational"
            StatusColor.YELLOW -> R.drawable.ic_notification_yellow to "Degraded Performance"
            StatusColor.RED -> R.drawable.ic_notification_red to "Outage Detected"
            StatusColor.BLACK -> R.drawable.ic_notification_black to "Feed Error"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(iconRes)
            .setContentTitle("DownNotice")
            .setContentText(title)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        NotificationManagerCompat.from(context).notify(STATUS_NOTIFICATION_ID, notification)
    }

    fun notifyNewIncidents(
        feedId: String,
        feedName: String,
        currentItems: List<FeedEntry>,
        previousItems: List<FeedEntry>
    ) {
        if (!hasNotificationPermission()) return

        val prevTitles = previousItems.map { it.title }.toSet()

        for (item in currentItems) {
            if (item.status == IncidentStatus.RESOLVED || item.status == IncidentStatus.SCHEDULED) continue

            val key = "$feedId:${item.title}"
            if (key in notifiedItems) continue
            if (item.title in prevTitles) continue

            notifiedItems.add(key)

            val iconRes = when (IncidentStatus.color(item.status)) {
                StatusColor.RED -> R.drawable.ic_notification_red
                StatusColor.YELLOW -> R.drawable.ic_notification_yellow
                else -> R.drawable.ic_notification_green
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, key.hashCode(), intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_INCIDENTS)
                .setSmallIcon(iconRes)
                .setContentTitle(feedName)
                .setContentText(item.title)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("${item.title}\n${item.description.take(200)}")
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(
                    if (item.status == IncidentStatus.DOWN) NotificationCompat.PRIORITY_HIGH
                    else NotificationCompat.PRIORITY_DEFAULT
                )
                .build()

            NotificationManagerCompat.from(context).notify(key.hashCode(), notification)
        }
    }

    fun processFeedResults(
        newResults: Map<String, FeedResult>,
        previousResults: Map<String, FeedResult>
    ) {
        for ((id, result) in newResults) {
            val prev = previousResults[id]
            notifyNewIncidents(id, result.name, result.items, prev?.items ?: emptyList())
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
