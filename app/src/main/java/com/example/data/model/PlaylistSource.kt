package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistSource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val channelCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
