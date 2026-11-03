package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: Long,
    val title: String,
    val logoUrl: String = "",
    val streamUrl: String,
    val groupTitle: String = "TV",
    val playbackPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val watchedAt: Long = System.currentTimeMillis()
)
