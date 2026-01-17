package com.voicetasker.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.voicetasker.features.auth.data.repository.FirebaseAuthRepository
import com.voicetasker.features.task.data.repository.FirebaseTaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * This module provides singleton-scoped dependencies that are shared across the app.
 *
 * Now uses Firebase repositories for real backend.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the FirebaseAuthRepository.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth
    ): FirebaseAuthRepository {
        return FirebaseAuthRepository(firebaseAuth)
    }

    /**
     * Provides the FirebaseTaskRepository.
     */
    @Provides
    @Singleton
    fun provideTaskRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): FirebaseTaskRepository {
        return FirebaseTaskRepository(firestore, firebaseAuth)
    }
}
