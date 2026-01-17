package com.voicetasker.features.auth.data.repository

import com.voicetasker.core.model.AuthResponse
import com.voicetasker.core.model.LoginRequest
import com.voicetasker.core.model.RegisterRequest
import com.voicetasker.core.network.api.AuthApi
import com.voicetasker.core.database.dao.UserDao
import javax.inject.Inject

/**
 * Repository for authentication operations.
 * Handles login, registration, and token management.
 */
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao
) {
    /**
     * Register a new user.
     */
    suspend fun register(request: RegisterRequest): Result<AuthResponse> = try {
        val response = authApi.register(request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Login an existing user.
     */
    suspend fun login(request: LoginRequest): Result<AuthResponse> = try {
        val response = authApi.login(request)
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Refresh authentication token.
     */
    suspend fun refreshToken(): Result<AuthResponse> = try {
        val response = authApi.refreshToken()
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Logout the current user.
     */
    suspend fun logout(userId: String): Result<Unit> = try {
        authApi.logout()
        userDao.logoutUser(userId)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
