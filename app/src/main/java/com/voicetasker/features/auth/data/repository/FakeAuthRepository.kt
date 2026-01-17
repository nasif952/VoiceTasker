package com.voicetasker.features.auth.data.repository

import com.voicetasker.core.model.AuthResponse
import com.voicetasker.core.model.LoginRequest
import com.voicetasker.core.model.RegisterRequest
import com.voicetasker.core.model.User
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/**
 * Fake implementation of AuthRepository for testing without a real backend.
 * Simulates successful and failed authentication responses.
 *
 * In production, this will be replaced with the real AuthRepository that calls actual APIs.
 */
class FakeAuthRepository {

    // Simulated database of registered users
    private val registeredUsers = mutableMapOf(
        "demo@example.com" to RegisterRequest(
            email = "demo@example.com",
            password = "password123",
            name = "Demo User"
        )
    )

    /**
     * Simulates user registration.
     * Success: New users can register
     * Failure: Email already exists
     */
    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        // Simulate network delay
        delay(1500)

        // Check if email already exists
        if (registeredUsers.containsKey(request.email)) {
            return Result.failure(Exception("Email already registered"))
        }

        // Simulate potential validation failures
        if (request.email.contains("test")) {
            return Result.failure(Exception("This email is reserved"))
        }

        // Register the user
        registeredUsers[request.email] = request

        // Return success response
        return Result.success(
            AuthResponse(
                accessToken = "fake_access_token_${System.currentTimeMillis()}",
                refreshToken = "fake_refresh_token_${System.currentTimeMillis()}",
                user = User(
                    id = "user_${System.nanoTime()}",
                    email = request.email,
                    name = request.name,
                    phone = null,
                    profilePictureUrl = null,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString(),
                    languagePreference = "en"
                )
            )
        )
    }

    /**
     * Simulates user login.
     * Success: Correct email + password
     * Failure: Invalid credentials or unregistered user
     */
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        // Simulate network delay
        delay(1500)

        // Check if user exists
        val user = registeredUsers[request.email]
        if (user == null) {
            return Result.failure(Exception("User not found"))
        }

        // Check password
        if (user.password != request.password) {
            return Result.failure(Exception("Invalid password"))
        }

        // Successful login
        return Result.success(
            AuthResponse(
                accessToken = "fake_access_token_${System.currentTimeMillis()}",
                refreshToken = "fake_refresh_token_${System.currentTimeMillis()}",
                user = User(
                    id = "user_${request.email.hashCode()}",
                    email = user.email,
                    name = user.name,
                    phone = null,
                    profilePictureUrl = null,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString(),
                    languagePreference = "en"
                )
            )
        )
    }

    /**
     * Simulates token refresh.
     * Always succeeds with new tokens.
     */
    suspend fun refreshToken(): Result<AuthResponse> {
        delay(500)

        return Result.success(
            AuthResponse(
                accessToken = "fake_access_token_refreshed_${System.currentTimeMillis()}",
                refreshToken = "fake_refresh_token_refreshed_${System.currentTimeMillis()}",
                user = User(
                    id = "user_demo",
                    email = "demo@example.com",
                    name = "Demo User",
                    phone = null,
                    profilePictureUrl = null,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString(),
                    languagePreference = "en"
                )
            )
        )
    }

    /**
     * Simulates logout.
     * Always succeeds.
     */
    suspend fun logout(userId: String): Result<Unit> {
        delay(500)
        return Result.success(Unit)
    }

    /**
     * Helper function to get registered users (for testing).
     */
    fun getRegisteredUsers(): Map<String, RegisterRequest> {
        return registeredUsers.toMap()
    }

    /**
     * Helper function to reset registered users (for testing).
     */
    fun resetUsers() {
        registeredUsers.clear()
        registeredUsers["demo@example.com"] = RegisterRequest(
            email = "demo@example.com",
            password = "password123",
            name = "Demo User"
        )
    }
}
