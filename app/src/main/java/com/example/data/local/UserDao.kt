package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount)

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)

    @Query("UPDATE users SET validUntilTimestamp = :validUntil, planType = :planType, planTitle = :planTitle, isActive = 1 WHERE username = :username")
    suspend fun extendSubscription(username: String, validUntil: Long, planType: String, planTitle: String)

    @Query("UPDATE users SET isActive = :isActive WHERE username = :username")
    suspend fun setUserActiveStatus(username: String, isActive: Boolean)
}
