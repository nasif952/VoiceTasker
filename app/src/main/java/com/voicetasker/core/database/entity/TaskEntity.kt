package com.voicetasker.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Task entity for Room database.
 * Represents a user-created task.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "status")
    val status: String, // TODO, IN_PROGRESS, COMPLETED, CANCELLED

    @ColumnInfo(name = "priority")
    val priority: String, // LOW, MEDIUM, HIGH

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime,

    @ColumnInfo(name = "due_date")
    val dueDate: LocalDateTime?,

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = false,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: LocalDateTime?,

    @ColumnInfo(name = "voice_transcription")
    val voiceTranscription: String?,

    @ColumnInfo(name = "ai_extracted_intent")
    val aiExtractedIntent: String?,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String, // PENDING, SYNCED, FAILED

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false
)
