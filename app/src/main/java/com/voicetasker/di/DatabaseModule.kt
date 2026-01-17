package com.voicetasker.di

import android.content.Context
import androidx.room.Room
import com.voicetasker.core.database.VoiceTaskerDatabase
import com.voicetasker.core.database.dao.TaskDao
import com.voicetasker.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database-related dependencies.
 * Provides Room database and DAO instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideVoiceTaskerDatabase(
        @ApplicationContext context: Context
    ): VoiceTaskerDatabase {
        return Room.databaseBuilder(
            context,
            VoiceTaskerDatabase::class.java,
            VoiceTaskerDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideTaskDao(database: VoiceTaskerDatabase): TaskDao {
        return database.taskDao()
    }

    @Singleton
    @Provides
    fun provideUserDao(database: VoiceTaskerDatabase): UserDao {
        return database.userDao()
    }
}
