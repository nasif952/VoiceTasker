package com.voicetasker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.voicetasker.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Data Access Object for User entity.
 * Provides database operations for user data and authentication.
 */
@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE is_logged_in = 1 LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Query("UPDATE users SET auth_token = :authToken, refresh_token = :refreshToken WHERE id = :userId")
    suspend fun updateUserTokens(userId: String, authToken: String?, refreshToken: String?)

    @Query("UPDATE users SET is_logged_in = 0 WHERE id = :userId")
    suspend fun logoutUser(userId: String)

    @Query("UPDATE users SET last_sync = :lastSync WHERE id = :userId")
    suspend fun updateLastSync(userId: String, lastSync: LocalDateTime)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)
}
