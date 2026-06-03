package com.example.vibecheck_dev.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vibecheck_dev.data.local.UserPreferences
import com.example.vibecheck_dev.data.source.remote.dto.RegisterRequest
import com.example.vibecheck_dev.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUsername: StateFlow<String> = userPreferences.playerNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val currentEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email ?: "NO_EMAIL_LINKED"

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)

            result.onSuccess { token ->
                userPreferences.saveAuthSession(token, true)
                userPreferences.savePlayerName(email.substringBefore("@"))
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Login gagal. Cek email/password.")
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val request = RegisterRequest(username, email, password)
            val result = repository.register(request)

            result.onSuccess {
                login(email, password)
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Register gagal ke server.")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.loginWithGoogle(idToken)

            result.onSuccess { uid ->
                val user = FirebaseAuth.getInstance().currentUser
                val googleName = user?.displayName ?: "GoogleUser"

                userPreferences.saveAuthSession("firebase_auth_active", true)
                userPreferences.savePlayerName(googleName)
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "Google Login Gagal.")
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.updateUsername(newUsername)

            result.onSuccess {
                userPreferences.savePlayerName(newUsername)
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "ERR: Gagal ganti username")
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.updatePassword(oldPassword, newPassword)

            result.onSuccess {
                _authState.value = AuthState.Success
            }.onFailure { e ->
                _authState.value = AuthState.Error(e.message ?: "ERR: Gagal ganti password")
            }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            userPreferences.saveAuthSession("", false)
            userPreferences.savePlayerName("")
            _authState.value = AuthState.Idle
            onLogoutComplete()
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}