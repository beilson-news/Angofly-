package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.Channel
import com.example.data.model.PlaylistSource
import com.example.data.model.WatchHistory
import com.example.data.parser.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class IptvRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val channelDao = db.channelDao()
    private val playlistDao = db.playlistDao()
    private val historyDao = db.watchHistoryDao()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    val allChannels: Flow<List<Channel>> = channelDao.getAllChannels()
    val favoriteChannels: Flow<List<Channel>> = channelDao.getFavoriteChannels()
    val allPlaylists: Flow<List<PlaylistSource>> = playlistDao.getAllPlaylists()
    val recentHistory: Flow<List<WatchHistory>> = historyDao.getRecentHistory()

    fun getChannelsByCategoryType(categoryType: String): Flow<List<Channel>> =
        channelDao.getChannelsByCategoryType(categoryType)

    fun getChannelsByGroup(groupTitle: String): Flow<List<Channel>> =
        channelDao.getChannelsByGroup(groupTitle)

    fun searchChannels(query: String): Flow<List<Channel>> =
        channelDao.searchChannels(query)

    suspend fun syncPlaylist(playlistId: Long = 1L, customUrl: String? = null): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val playlist = playlistDao.getPlaylistById(playlistId)
            val url = customUrl ?: playlist?.url ?: "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/M3U/M3UPT.m3u"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) ANGOFLY/1.0 IPTV")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // If remote fetch failed, check if we already have channels
                val existingCount = channelDao.getChannelCount()
                if (existingCount == 0) {
                    val fallbacks = M3UParser.getEmergencyFallbackChannels(playlistId)
                    channelDao.insertChannels(fallbacks)
                    return@withContext Result.success(fallbacks.size)
                }
                return@withContext Result.failure(Exception("HTTP Erro ${response.code}"))
            }

            val bodyString = response.body?.string() ?: ""
            val parsedChannels = M3UParser.parse(bodyString, playlistId)

            if (parsedChannels.isNotEmpty()) {
                channelDao.deleteChannelsByPlaylist(playlistId)
                channelDao.insertChannels(parsedChannels)
                playlistDao.updateChannelCount(playlistId, parsedChannels.size)
                Result.success(parsedChannels.size)
            } else {
                val fallbacks = M3UParser.getEmergencyFallbackChannels(playlistId)
                channelDao.insertChannels(fallbacks)
                Result.success(fallbacks.size)
            }
        } catch (e: Exception) {
            Log.e("IptvRepository", "Sync failed, loading fallbacks if empty", e)
            val existingCount = channelDao.getChannelCount()
            if (existingCount == 0) {
                val fallbacks = M3UParser.getEmergencyFallbackChannels(playlistId)
                channelDao.insertChannels(fallbacks)
                Result.success(fallbacks.size)
            } else {
                Result.success(existingCount)
            }
        }
    }

    suspend fun toggleFavorite(channelId: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        channelDao.setFavorite(channelId, !currentFavorite)
    }

    suspend fun recordHistory(channel: Channel, positionMs: Long = 0L, durationMs: Long = 0L) = withContext(Dispatchers.IO) {
        historyDao.insertOrUpdate(
            WatchHistory(
                channelId = channel.id,
                title = channel.name,
                logoUrl = channel.logoUrl,
                streamUrl = channel.streamUrl,
                groupTitle = channel.groupTitle,
                playbackPositionMs = positionMs,
                durationMs = durationMs,
                watchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addCustomPlaylist(name: String, url: String): Long = withContext(Dispatchers.IO) {
        val newId = playlistDao.insertPlaylist(
            PlaylistSource(
                name = name,
                url = url,
                isDefault = false,
                isActive = true
            )
        )
        syncPlaylist(newId, url)
        newId
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearHistory()
    }
}
