package com.downnotice.mobile.data.repository

import android.content.Context
import com.downnotice.mobile.data.model.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class SettingsRepository(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    private val settingsFile: File = File(context.filesDir, "settings.json")

    private var cached: AppSettings? = null

    suspend fun load(): AppSettings = withContext(Dispatchers.IO) {
        cached?.let { return@withContext it }
        val settings = if (settingsFile.exists()) {
            try {
                json.decodeFromString<AppSettings>(settingsFile.readText())
            } catch (_: Exception) {
                AppSettings()
            }
        } else {
            AppSettings().also { save(it) }
        }
        cached = settings
        settings
    }

    suspend fun save(settings: AppSettings) = withContext(Dispatchers.IO) {
        cached = settings
        settingsFile.parentFile?.mkdirs()
        settingsFile.writeText(json.encodeToString(AppSettings.serializer(), settings))
    }

    fun loadSync(): AppSettings {
        cached?.let { return it }
        val settings = if (settingsFile.exists()) {
            try {
                json.decodeFromString<AppSettings>(settingsFile.readText())
            } catch (_: Exception) {
                AppSettings()
            }
        } else {
            AppSettings()
        }
        cached = settings
        return settings
    }
}
