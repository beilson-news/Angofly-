package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val prefs = context.getSharedPreferences("angofly_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    val allUsers: Flow<List<UserAccount>> = userDao.getAllUsers()

    suspend fun checkPersistedSession(): UserAccount? = withContext(Dispatchers.IO) {
        val savedUsername = prefs.getString("logged_username", null)
        if (!savedUsername.isNullOrEmpty()) {
            val user = userDao.getUserByUsername(savedUsername)
            if (user != null && user.isActive) {
                _currentUser.value = user
                return@withContext user
            }
        }
        return@withContext null
    }

    suspend fun login(username: String, password: String):Result<UserAccount> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val cleanPass = password.trim()

        var user = userDao.getUserByUsername(cleanUser)

        // Seed demo account on the fly if needed
        if (user == null && cleanUser.equals("DEMO001", ignoreCase = true) && cleanPass == "Demo@12345") {
            val demo = UserAccount(
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
            userDao.insertUser(demo)
            user = demo
        }

        // Seed admin account on the fly if needed
        if (user == null && cleanUser.equals("ADMIN", ignoreCase = true) && cleanPass == "Admin@Angofly2026") {
            val admin = UserAccount(
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
            userDao.insertUser(admin)
            user = admin
        }

        if (user == null) {
            return@withContext Result.failure(Exception("Utilizador não encontrado. Verifique as credenciais."))
        }

        if (user.passwordHash != cleanPass) {
            return@withContext Result.failure(Exception("Senha incorreta. Tente novamente."))
        }

        if (!user.isActive) {
            return@withContext Result.failure(Exception("Esta conta está bloqueada pelo administrador. Contacte o suporte."))
        }

        // Save session
        prefs.edit().putString("logged_username", user.username).apply()
        _currentUser.value = user
        Result.success(user)
    }

    suspend fun register(username: String, pass: String, fullName: String, email: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim()
        val existing = userDao.getUserByUsername(cleanUser)
        if (existing != null) {
            return@withContext Result.failure(Exception("Este nome de utilizador já existe. Escolha outro."))
        }

        val newUser = UserAccount(
            username = cleanUser,
            passwordHash = pass.trim(),
            fullName = fullName.trim(),
            email = email.trim(),
            planType = "TESTE",
            planTitle = "Teste Grátis (24 Horas)",
            validUntilTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
            isActive = true,
            isDemo = false,
            isAdmin = false
        )
        userDao.insertUser(newUser)
        prefs.edit().putString("logged_username", newUser.username).apply()
        _currentUser.value = newUser
        Result.success(newUser)
    }

    suspend fun recoverPassword(username: String): Result<String> = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username.trim())
        if (user == null) {
            Result.failure(Exception("Utilizador não cadastrado."))
        } else {
            Result.success("Instruções de recuperação e código temporário enviados para ${user.email.ifEmpty { "seu telemóvel/email associado" }}.")
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.edit().remove("logged_username").apply()
        _currentUser.value = null
    }

    suspend fun extendUserPlan(username: String, daysToAdd: Int, planTitle: String) = withContext(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username) ?: return@withContext
        val baseTime = if (user.validUntilTimestamp > System.currentTimeMillis()) user.validUntilTimestamp else System.currentTimeMillis()
        val newValid = baseTime + (daysToAdd * 24L * 60 * 60 * 1000L)
        userDao.extendSubscription(username, newValid, "PREMIUM_${daysToAdd}D", planTitle)
        if (_currentUser.value?.username == username) {
            _currentUser.value = userDao.getUserByUsername(username)
        }
    }

    suspend fun toggleUserActive(username: String, isActive: Boolean) = withContext(Dispatchers.IO) {
        userDao.setUserActiveStatus(username, isActive)
        if (_currentUser.value?.username == username) {
            _currentUser.value = userDao.getUserByUsername(username)
        }
    }
}
