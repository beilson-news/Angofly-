package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PaymentReceipt
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserAccount
import com.example.data.repository.AuthRepository
import com.example.data.repository.PaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val paymentRepo = PaymentRepository(application, authRepo)

    val currentUser: StateFlow<UserAccount?> = authRepo.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    val allUsers: StateFlow<List<UserAccount>> = authRepo.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<PaymentReceipt>> = paymentRepo.allReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _supportWhatsAppNumber = MutableStateFlow("+244923000111")
    val supportWhatsAppNumber: StateFlow<String> = _supportWhatsAppNumber.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.checkPersistedSession()
        }
    }

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Preencha o utilizador e a senha.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepo.login(username, pass)
            result.onSuccess {
                _authState.value = AuthState.Success(it)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Falha no início de sessão.")
            }
        }
    }

    fun register(username: String, pass: String, fullName: String, email: String) {
        if (username.isBlank() || pass.isBlank() || fullName.isBlank()) {
            _authState.value = AuthState.Error("Preencha todos os campos obrigatórios.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepo.register(username, pass, fullName, email)
            result.onSuccess {
                _authState.value = AuthState.Success(it)
                _infoMessage.value = "Conta criada com sucesso! 24h de teste ativadas."
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Erro ao criar conta.")
            }
        }
    }

    fun recoverPassword(username: String) {
        if (username.isBlank()) {
            _infoMessage.value = "Insira o nome de utilizador para recuperar."
            return
        }
        viewModelScope.launch {
            val res = authRepo.recoverPassword(username)
            res.onSuccess {
                _infoMessage.value = it
            }.onFailure {
                _infoMessage.value = it.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun clearInfoMessage() {
        _infoMessage.value = null
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun submitPayment(
        plan: SubscriptionPlan,
        method: String,
        referenceOrPhone: String,
        transactionCode: String,
        proofNote: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            paymentRepo.submitPayment(
                username = user.username,
                plan = plan,
                method = method,
                referenceOrPhone = referenceOrPhone,
                transactionCode = transactionCode,
                proofNote = proofNote
            )
            _infoMessage.value = "Comprovativo enviado com sucesso! Aguarde a aprovação do administrador."
        }
    }

    fun approveReceipt(receipt: PaymentReceipt) {
        viewModelScope.launch {
            paymentRepo.approvePayment(receipt)
            _infoMessage.value = "Pagamento aprovado e plano ativado para @${receipt.username}!"
        }
    }

    fun rejectReceipt(receiptId: Long) {
        viewModelScope.launch {
            paymentRepo.rejectPayment(receiptId)
            _infoMessage.value = "Comprovativo rejeitado."
        }
    }

    fun extendUserManual(username: String, daysToAdd: Int, planTitle: String) {
        viewModelScope.launch {
            authRepo.extendUserPlan(username, daysToAdd, planTitle)
            _infoMessage.value = "Plano de @$username estendido por +$daysToAdd dias!"
        }
    }

    fun toggleUserBlock(username: String, currentActive: Boolean) {
        viewModelScope.launch {
            authRepo.toggleUserActive(username, !currentActive)
            _infoMessage.value = if (currentActive) "Utilizador @$username bloqueado." else "Utilizador @$username desbloqueado."
        }
    }

    fun updateWhatsAppNumber(newNumber: String) {
        _supportWhatsAppNumber.value = newNumber
        _infoMessage.value = "Número de suporte WhatsApp atualizado!"
    }
}
