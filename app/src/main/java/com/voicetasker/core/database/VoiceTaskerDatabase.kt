package com.voicetasker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.voicetasker.core.database.converter.LocalDateTimeConverter
import com.voicetasker.core.database.dao.TaskDao
import com.voicetasker.core.database.dao.UserDao
import com.voicetasker.core.database.entity.TaskEntity
import com.voicetasker.core.database.entity.UserEntity

/**
 * Room database for VoiceTasker.
 * Defines the database configuration and entities.
 */
@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class VoiceTaskerDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "voicetasker.db"
    }
}
