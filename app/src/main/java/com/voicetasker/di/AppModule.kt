package com.voicetasker.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for application-level dependencies.
 * This module provides singleton-scoped dependencies that are shared across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // TODO: Add network dependencies (Retrofit, OkHttp)
    // TODO: Add database dependencies (Room)
    // TODO: Add repository dependencies
    // TODO: Add authentication dependencies
}
