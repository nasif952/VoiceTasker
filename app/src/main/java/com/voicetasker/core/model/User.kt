package com.voicetasker.core.model

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Domain model for User.
 * Used throughout the app for business logic.
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val profilePictureUrl: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val languagePreference: String = "en"
)

/**
 * Request model for user registration.
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null
)

/**
 * Request model for user login.
 */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Response model for authentication.
 */
@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
