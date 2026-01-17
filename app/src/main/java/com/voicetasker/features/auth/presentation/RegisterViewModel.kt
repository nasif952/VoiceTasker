package com.voicetasker.features.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetasker.core.model.RegisterRequest
import com.voicetasker.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Register screen.
 * Manages registration state, form inputs, and user registration operations.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // UI State
    val name = mutableStateOf("")
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isRegisterSuccess = mutableStateOf(false)

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
     * Validates that name is not empty.
     */
    private fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2
    }

    /**
     * Validates that passwords match.
     */
    private fun passwordsMatch(): Boolean {
        return password.value == confirmPassword.value
    }

    /**
     * Attempts to register a new user.
     */
    fun register() {
        // Clear previous errors
        error.value = null

        // Validate inputs
        if (!isValidName(name.value)) {
            error.value = "Name must be at least 2 characters"
            return
        }

        if (!isValidEmail(email.value)) {
            error.value = "Invalid email format"
            return
        }

        if (!isValidPassword(password.value)) {
            error.value = "Password must be at least 6 characters"
            return
        }

        if (!passwordsMatch()) {
            error.value = "Passwords do not match"
            return
        }

        // Perform registration
        isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.register(
                RegisterRequest(
                    name = name.value,
                    email = email.value,
                    password = password.value
                )
            )

            isLoading.value = false

            result.onSuccess { authResponse ->
                // Registration successful
                isRegisterSuccess.value = true
            }

            result.onFailure { exception ->
                // Registration failed
                error.value = exception.message ?: "Registration failed. Please try again."
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
     * Resets the registration success state (used after navigation).
     */
    fun resetRegisterSuccess() {
        isRegisterSuccess.value = false
    }
}
