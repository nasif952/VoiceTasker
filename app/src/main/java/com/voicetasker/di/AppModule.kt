package com.voicetasker.di

import com.voicetasker.features.auth.data.repository.FakeAuthRepository
import com.voicetasker.features.task.data.repository.FakeTaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * This module provides singleton-scoped dependencies that are shared across the app.
 *
 * Currently uses Fake repositories for testing without a real backend.
 * To switch to real backend:
 * 1. Complete NetworkModule.kt with Retrofit setup
 * 2. Create real Repository instances
 * 3. Update the @Provides functions to return real implementations
 * 4. Remove Fake repositories
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the FakeAuthRepository for testing.
     *
     * Currently returns FakeAuthRepository for testing without a real backend.
     * After API setup is complete, replace with real AuthRepository.
     */
    @Provides
    @Singleton
    fun provideFakeAuthRepository(): FakeAuthRepository {
        return FakeAuthRepository()
    }

    /**
     * Provides the FakeTaskRepository for testing.
     *
     * Currently returns FakeTaskRepository for testing without a real backend.
     * After API setup is complete, replace with real TaskRepository.
     */
    @Provides
    @Singleton
    fun provideFakeTaskRepository(): FakeTaskRepository {
        return FakeTaskRepository()
    }
}
