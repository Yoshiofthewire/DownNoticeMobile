package com.downnotice.mobile.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.downnotice.mobile.data.repository.SettingsRepository
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            schedulePollWork(context)
        }
    }

    companion object {
        const val WORK_NAME = "downnotice_poll"

        fun schedulePollWork(context: Context, intervalMinutes: Int? = null) {
            val interval = intervalMinutes
                ?: SettingsRepository(context).loadSync().refreshInterval

            val workRequest = PeriodicWorkRequestBuilder<PollWorker>(
                interval.toLong().coerceAtLeast(15),
                TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
