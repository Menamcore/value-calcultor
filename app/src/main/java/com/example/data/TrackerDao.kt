package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM tracker_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<TrackerSettings?>

    @Query("SELECT * FROM tracker_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): TrackerSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: TrackerSettings)
}
