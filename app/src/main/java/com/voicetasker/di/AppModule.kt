package com.voicetasker.di

import com.voicetasker.features.auth.data.repository.FakeAuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 * This module provides singleton-scoped dependencies that are shared across the app.
 *
 * Currently uses FakeAuthRepository for testing without a real backend.
 * To switch to real backend:
 * 1. Complete NetworkModule.kt with Retrofit setup
 * 2. Create real AuthRepository instance
 * 3. Update the @Provides fakeAuthRepository function to return real implementation
 * 4. Remove FakeAuthRepository
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
}
