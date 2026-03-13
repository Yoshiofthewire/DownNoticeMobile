package com.downnotice.mobile.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.downnotice.mobile.data.repository.FeedRepository
import com.downnotice.mobile.data.repository.SettingsRepository
import com.downnotice.mobile.notification.NotificationHelper

class PollWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsRepo = SettingsRepository(applicationContext)
        val feedRepo = FeedRepository()
        val notificationHelper = NotificationHelper(applicationContext)

        return try {
            val settings = settingsRepo.load()
            val previousFeeds = feedRepo.getCachedFeeds()
            val feeds = feedRepo.fetchAllFeeds(settings.feeds, settings.historyHours)

            if (settings.notifications) {
                notificationHelper.processFeedResults(feeds, previousFeeds)
                notificationHelper.updateStatusNotification(feedRepo.getOverallStatus())
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
