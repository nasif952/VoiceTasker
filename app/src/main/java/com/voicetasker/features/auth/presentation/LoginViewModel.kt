package com.voicetasker.features.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetasker.core.model.LoginRequest
import com.voicetasker.features.auth.data.repository.FakeAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Login screen.
 * Manages login state, form inputs, and authentication operations.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: FakeAuthRepository
) : ViewModel() {

    // UI State
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isLoginSuccess = mutableStateOf(false)

    /**
     * Validates email format.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password (minimum 6 characters).
     */
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Attempts to login the user with the provided email and password.
     */
    fun login() {
        // Clear previous errors
        error.value = null

        // Validate inputs
        if (!isValidEmail(email.value)) {
            error.value = "Invalid email format"
            return
        }

        if (!isValidPassword(password.value)) {
            error.value = "Password must be at least 6 characters"
            return
        }

        // Perform login
        isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.login(
                LoginRequest(
                    email = email.value,
                    password = password.value
                )
            )

            isLoading.value = false

            result.onSuccess { authResponse ->
                // Login successful
                isLoginSuccess.value = true
            }

            result.onFailure { exception ->
                // Login failed
                error.value = exception.message ?: "Login failed. Please try again."
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Resets the login success state (used after navigation).
     */
    fun resetLoginSuccess() {
        isLoginSuccess.value = false
    }
}
