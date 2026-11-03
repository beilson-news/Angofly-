package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val email: String = "",
    val planType: String = "TESTE", // TESTE, PLANO_7D, PLANO_30D, PLANO_90D, PLANO_180D, PLANO_365D, ADMIN
    val planTitle: String = "Conta Demonstração (24h)",
    val validUntilTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val isActive: Boolean = true,
    val isDemo: Boolean = false,
    val isAdmin: Boolean = false,
    val deviceId: String = "ANG-ANDROID-${System.currentTimeMillis() % 100000}",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isExpired: Boolean
        get() = !isAdmin && System.currentTimeMillis() > validUntilTimestamp

    val daysRemaining: Long
        get() {
            if (isAdmin) return 9999L
            val diff = validUntilTimestamp - System.currentTimeMillis()
            return if (diff > 0) (diff / (24 * 60 * 60 * 1000L)) else 0L
        }

    val hoursRemaining: Long
        get() {
            if (isAdmin) return 99999L
            val diff = validUntilTimestamp - System.currentTimeMillis()
            return if (diff > 0) (diff / (60 * 60 * 1000L)) else 0L
        }
}
