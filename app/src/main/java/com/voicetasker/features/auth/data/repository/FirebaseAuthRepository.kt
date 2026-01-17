package com.voicetasker.features.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of AuthRepository.
 * Handles authentication using Firebase Auth.
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    /**
     * Get current logged in user.
     */
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Check if user is logged in.
     */
    val isLoggedIn: Boolean
        get() = currentUser != null

    /**
     * Login with email and password.
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Login failed: User is null"))
        } catch (e: Exception) {
            Result.failure(Exception(getFirebaseErrorMessage(e)))
        }
    }

    /**
     * Register with email and password.
     */
    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Registration failed: User is null"))
        } catch (e: Exception) {
            Result.failure(Exception(getFirebaseErrorMessage(e)))
        }
    }

    /**
     * Logout current user.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Send password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(getFirebaseErrorMessage(e)))
        }
    }

    /**
     * Convert Firebase exceptions to user-friendly messages.
     */
    private fun getFirebaseErrorMessage(exception: Exception): String {
        val message = exception.message ?: "Unknown error"
        return when {
            message.contains("INVALID_LOGIN_CREDENTIALS") ||
            message.contains("INVALID_EMAIL") ||
            message.contains("wrong-password") -> "Invalid email or password"

            message.contains("EMAIL_EXISTS") ||
            message.contains("email-already-in-use") -> "Email already registered"

            message.contains("WEAK_PASSWORD") ||
            message.contains("weak-password") -> "Password is too weak (min 6 characters)"

            message.contains("USER_NOT_FOUND") ||
            message.contains("user-not-found") -> "No account found with this email"

            message.contains("TOO_MANY_REQUESTS") ||
            message.contains("too-many-requests") -> "Too many attempts. Please try again later"

            message.contains("NETWORK") ||
            message.contains("network") -> "Network error. Check your connection"

            else -> message
        }
    }
}
