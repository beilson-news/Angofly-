package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Channel
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY id ASC")
    fun getAllChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE categoryType = :categoryType ORDER BY id ASC")
    fun getChannelsByCategoryType(categoryType: String): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE groupTitle = :groupTitle ORDER BY id ASC")
    fun getChannelsByGroup(groupTitle: String): Flow<List<Channel>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE groupTitle != '' ORDER BY groupTitle ASC")
    fun getDistinctGroups(): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: Long): Channel?

    @Query("SELECT COUNT(*) FROM channels")
    suspend fun getChannelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel): Long

    @Update
    suspend fun updateChannel(channel: Channel)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}
