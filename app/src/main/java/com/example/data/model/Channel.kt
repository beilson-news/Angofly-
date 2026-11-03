package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val groupTitle: String = "TV",
    val tvgId: String = "",
    val userAgent: String = "",
    val referrer: String = "",
    val isFavorite: Boolean = false,
    val playlistId: Long = 1L,
    val categoryType: String = "LIVE", // LIVE, MOVIE, SERIES, RADIO
    val currentProgram: String = "Transmissão Oficial em Direto",
    val nextProgram: String = "Programação Especial ANGOFLY"
)
