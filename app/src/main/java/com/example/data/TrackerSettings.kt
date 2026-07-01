package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracker_settings")
data class TrackerSettings(
    @PrimaryKey val id: Int = 1,
    val lydInBank: Double = 0.0,
    val lydInCash: Double = 0.0,
    val usdInCash: Double = 0.0,
    val cashRate: Double = 6.0,          // Reasonable default black market cash rate
    val bankBalanceRate: Double = 7.5     // Reasonable default bank balance rate
)
