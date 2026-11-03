package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentReceipt
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<PaymentReceipt>>

    @Query("SELECT * FROM payment_receipts WHERE username = :username ORDER BY createdAt DESC")
    fun getReceiptsByUser(username: String): Flow<List<PaymentReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PaymentReceipt): Long

    @Update
    suspend fun updateReceipt(receipt: PaymentReceipt)

    @Query("UPDATE payment_receipts SET status = :status, approvedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM payment_receipts WHERE id = :id")
    suspend fun deleteReceipt(id: Long)
}
