package com.example.cryptoportfolio.ui.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.util.PasswordUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Login state
    var loginEmail by mutableStateOf("")
        private set
    var loginPassword by mutableStateOf("")
        private set

    // Register state
    var registerUsername by mutableStateOf("")
        private set
    var registerEmail by mutableStateOf("")
        private set
    var registerPassword by mutableStateOf("")
        private set
    var registerConfirmPassword by mutableStateOf("")
        private set

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUserEmail = authRepository.currentEmail
    val currentUsername = authRepository.currentUsername

    // Update functions
    fun updateLoginEmail(email: String) {
        loginEmail = email
    }

    fun updateLoginPassword(password: String) {
        loginPassword = password
    }

    fun updateRegisterUsername(username: String) {
        registerUsername = username
    }

    fun updateRegisterEmail(email: String) {
        registerEmail = email
    }

    fun updateRegisterPassword(password: String) {
        registerPassword = password
    }

    fun updateRegisterConfirmPassword(password: String) {
        registerConfirmPassword = password
    }

    // Login
    fun login() {
        viewModelScope.launch {
            if (!validateLoginInput()) return@launch

            _isLoading.value = true
            _authState.value = AuthState.Loading

            val result = authRepository.login(loginEmail.trim(), loginPassword)

            result.onSuccess {
                _authState.value = AuthState.Success("Login successful")
                clearLoginFields()
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Login failed")
            }

            _isLoading.value = false
        }
    }

    // Register
    fun register() {
        viewModelScope.launch {
            if (!validateRegisterInput()) return@launch

            _isLoading.value = true
            _authState.value = AuthState.Loading

            val result = authRepository.register(
                registerUsername.trim(),
                registerEmail.trim(),
                registerPassword
            )

            result.onSuccess {
                _authState.value = AuthState.Success("Registration successful")
                clearRegisterFields()
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Registration failed")
            }

            _isLoading.value = false
        }
    }

    // Logout
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
        }
    }

    // Validation
    private fun validateLoginInput(): Boolean {
        if (loginEmail.isBlank()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches()) {
            _authState.value = AuthState.Error("Invalid email format")
            return false
        }
        if (loginPassword.isBlank()) {
            _authState.value = AuthState.Error("Password cannot be empty")
            return false
        }
        return true
    }

    private fun validateRegisterInput(): Boolean {
        if (registerUsername.isBlank()) {
            _authState.value = AuthState.Error("Username cannot be empty")
            return false
        }
        if (registerEmail.isBlank()) {
            _authState.value = AuthState.Error("Email cannot be empty")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(registerEmail).matches()) {
            _authState.value = AuthState.Error("Invalid email format")
            return false
        }
        if (!PasswordUtil.validatePassword(registerPassword)) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return false
        }
        if (registerPassword != registerConfirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return false
        }
        return true
    }

    // Clear fields
    private fun clearLoginFields() {
        loginEmail = ""
        loginPassword = ""
    }

    private fun clearRegisterFields() {
        registerUsername = ""
        registerEmail = ""
        registerPassword = ""
        registerConfirmPassword = ""
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}