package com.voicetasker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * User entity for Room database.
 * Stores local user information and auth tokens.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "phone")
    val phone: String?,

    @ColumnInfo(name = "profile_picture_url")
    val profilePictureUrl: String?,

    @ColumnInfo(name = "auth_token")
    val authToken: String?, // Encrypted JWT or session token

    @ColumnInfo(name = "refresh_token")
    val refreshToken: String?, // Encrypted refresh token

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime,

    @ColumnInfo(name = "last_sync")
    val lastSync: LocalDateTime?,

    @ColumnInfo(name = "is_logged_in")
    val isLoggedIn: Boolean = true,

    @ColumnInfo(name = "sync_enabled")
    val syncEnabled: Boolean = true,

    @ColumnInfo(name = "language_preference")
    val languagePreference: String = "en" // ISO 639-1 code
)
