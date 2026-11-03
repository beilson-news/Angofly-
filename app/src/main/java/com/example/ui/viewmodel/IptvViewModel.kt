package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Channel
import com.example.data.model.PlaylistSource
import com.example.data.model.WatchHistory
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = IptvRepository(application)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val allChannels: StateFlow<List<Channel>> = repo.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<Channel>> = repo.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistSource>> = repo.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentHistory: StateFlow<List<WatchHistory>> = repo.recentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Todos")

    // Filtered channels based on search and selected category
    val filteredChannels: StateFlow<List<Channel>> = combine(
        allChannels,
        searchQuery,
        selectedCategory
    ) { channels, query, category ->
        channels.filter { channel ->
            val matchesQuery = query.isBlank() ||
                    channel.name.contains(query, ignoreCase = true) ||
                    channel.groupTitle.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "Todos" -> true
                "TV ao Vivo" -> channel.categoryType == "LIVE"
                "Filmes" -> channel.categoryType == "MOVIE"
                "Séries" -> channel.categoryType == "SERIES"
                "Rádios" -> channel.categoryType == "RADIO"
                "Favoritos" -> channel.isFavorite
                else -> channel.groupTitle.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Movie channels specifically
    val movieChannels: StateFlow<List<Channel>> = allChannels.combine(searchQuery) { channels, query ->
        channels.filter { it.categoryType == "MOVIE" && (query.isBlank() || it.name.contains(query, ignoreCase = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Series channels specifically
    val seriesChannels: StateFlow<List<Channel>> = allChannels.combine(searchQuery) { channels, query ->
        channels.filter { (it.categoryType == "SERIES" || it.groupTitle.contains("Série", ignoreCase = true) || it.groupTitle.contains("Eixo do Mal", ignoreCase = true)) && (query.isBlank() || it.name.contains(query, ignoreCase = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live TV channels
    val liveTvChannels: StateFlow<List<Channel>> = allChannels.combine(searchQuery) { channels, query ->
        channels.filter { it.categoryType == "LIVE" && (query.isBlank() || it.name.contains(query, ignoreCase = true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active player channel
    private val _currentPlayingChannel = MutableStateFlow<Channel?>(null)
    val currentPlayingChannel: StateFlow<Channel?> = _currentPlayingChannel.asStateFlow()

    init {
        // Automatically sync default playlist on app startup
        syncDefaultPlaylist(force = false)
    }

    fun syncDefaultPlaylist(force: Boolean = false) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repo.syncPlaylist(playlistId = 1L)
            result.onSuccess { count ->
                _syncMessage.value = "Playlist sincronizada com $count canais disponíveis!"
            }.onFailure { err ->
                _syncMessage.value = "Falha ao atualizar playlist online. Canais locais ativos."
            }
            _isSyncing.value = false
        }
    }

    fun playChannel(channel: Channel) {
        _currentPlayingChannel.value = channel
        viewModelScope.launch {
            repo.recordHistory(channel)
        }
    }

    fun stopPlayback() {
        _currentPlayingChannel.value = null
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repo.toggleFavorite(channel.id, channel.isFavorite)
        }
    }

    fun addCustomPlaylist(name: String, url: String) {
        if (name.isBlank() || url.isBlank()) return
        viewModelScope.launch {
            _isSyncing.value = true
            val id = repo.addCustomPlaylist(name, url)
            _syncMessage.value = "Playlist '$name' adicionada com sucesso!"
            _isSyncing.value = false
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repo.deletePlaylist(playlistId)
            _syncMessage.value = "Playlist removida."
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repo.clearHistory()
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }
}
