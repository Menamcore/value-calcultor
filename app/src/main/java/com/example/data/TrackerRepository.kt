package com.example.data

import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val trackerDao: TrackerDao) {
    val settingsFlow: Flow<TrackerSettings?> = trackerDao.getSettingsFlow()

    suspend fun getSettingsDirect(): TrackerSettings? {
        return trackerDao.getSettingsDirect()
    }

    suspend fun saveSettings(settings: TrackerSettings) {
        trackerDao.insertSettings(settings)
    }
}
