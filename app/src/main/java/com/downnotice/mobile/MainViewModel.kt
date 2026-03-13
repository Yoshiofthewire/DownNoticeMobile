package com.downnotice.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.downnotice.mobile.data.model.AppSettings
import com.downnotice.mobile.data.model.FeedResult
import com.downnotice.mobile.data.model.IncidentStatus
import com.downnotice.mobile.data.repository.FeedRepository
import com.downnotice.mobile.data.repository.SettingsRepository
import com.downnotice.mobile.notification.NotificationHelper
import com.downnotice.mobile.service.BootReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val feedRepo = FeedRepository()
    private val notificationHelper = NotificationHelper(application)

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _feeds = MutableStateFlow<Map<String, FeedResult>>(emptyMap())
    val feeds: StateFlow<Map<String, FeedResult>> = _feeds.asStateFlow()

    private val _overallStatus = MutableStateFlow(IncidentStatus.OPERATIONAL)
    val overallStatus: StateFlow<IncidentStatus> = _overallStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = settingsRepo.load()
            refreshFeeds()
        }
    }

    fun refreshFeeds() {
        viewModelScope.launch {
            _isLoading.value = true
            val settings = _settings.value
            val previousFeeds = feedRepo.getCachedFeeds()
            val results = feedRepo.fetchAllFeeds(settings.feeds, settings.historyHours)
            _feeds.value = results
            _overallStatus.value = feedRepo.getOverallStatus()

            if (settings.notifications) {
                notificationHelper.processFeedResults(results, previousFeeds)
                notificationHelper.updateStatusNotification(feedRepo.getOverallStatus())
            }
            _isLoading.value = false
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsRepo.save(newSettings)
            _settings.value = newSettings
            BootReceiver.schedulePollWork(
                getApplication(),
                newSettings.refreshInterval
            )
        }
    }
}
