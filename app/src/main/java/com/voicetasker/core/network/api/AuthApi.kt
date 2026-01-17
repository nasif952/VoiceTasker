package com.voicetasker.core.network.api

import com.voicetasker.core.model.AuthResponse
import com.voicetasker.core.model.LoginRequest
import com.voicetasker.core.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for authentication endpoints.
 * TODO: Update base URL to point to actual Supabase backend
 */
interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refreshToken(): AuthResponse

    @POST("auth/logout")
    suspend fun logout()
}
