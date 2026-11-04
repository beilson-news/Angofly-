package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Channel
import com.example.data.model.PaymentReceipt
import com.example.data.model.PlaylistSource
import com.example.data.model.UserAccount
import com.example.data.model.WatchHistory
import com.example.data.parser.M3UParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Channel::class,
        PlaylistSource::class,
        UserAccount::class,
        WatchHistory::class,
        PaymentReceipt::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun userDao(): UserDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "angofly_iptv.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default accounts, playlists and core verified channels
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.playlistDao().insertPlaylist(
                                    PlaylistSource(
                                        name = "M3UPT Oficial (Portugal & Lusofonia)",
                                        url = "https://raw.githubusercontent.com/LITUATUI/M3UPT/main/M3U/M3UPT.m3u",
                                        isDefault = true,
                                        isActive = true
                                    )
                                )
                                // Pre-seed verified channels
                                database.channelDao().insertChannels(M3UParser.getEmergencyFallbackChannels(1L))

                                // Demo Account
                                database.userDao().insertUser(
                                    UserAccount(
                                        username = "DEMO001",
                                        passwordHash = "Demo@12345",
                                        fullName = "Cliente Demonstração",
                                        email = "demo@angofly.ao",
                                        planType = "TESTE",
                                        planTitle = "Conta Demonstração (24h)",
                                        validUntilTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
                                        isActive = true,
                                        isDemo = true,
                                        isAdmin = false
                                    )
                                )
                                // Admin Account
                                database.userDao().insertUser(
                                    UserAccount(
                                        username = "ADMIN",
                                        passwordHash = "Admin@Angofly2026",
                                        fullName = "Administrador ANGOFLY",
                                        email = "admin@angofly.ao",
                                        planType = "ADMIN",
                                        planTitle = "Acesso Total Administrador",
                                        validUntilTimestamp = Long.MAX_VALUE,
                                        isActive = true,
                                        isDemo = false,
                                        isAdmin = true
                                    )
                                )
                                // Premium VIP Account
                                database.userDao().insertUser(
                                    UserAccount(
                                        username = "VIP001",
                                        passwordHash = "Vip@2026",
                                        fullName = "Assinante Premium VIP",
                                        email = "vip@angofly.ao",
                                        planType = "PLANO_365D",
                                        planTitle = "Plano Anual VIP (365 Dias)",
                                        validUntilTimestamp = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000L),
                                        isActive = true,
                                        isDemo = false,
                                        isAdmin = false
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
