package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlaylistSource
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY isDefault DESC, id ASC")
    fun getAllPlaylists(): Flow<List<PlaylistSource>>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlaylist(): PlaylistSource?

    @Query("SELECT * FROM playlists WHERE id = :id LIMIT 1")
    suspend fun getPlaylistById(id: Long): PlaylistSource?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistSource): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistSource)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("UPDATE playlists SET isActive = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setActivePlaylist(id: Long)

    @Query("UPDATE playlists SET channelCount = :count, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateChannelCount(id: Long, count: Int, timestamp: Long = System.currentTimeMillis())
}
