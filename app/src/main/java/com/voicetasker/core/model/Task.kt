package com.voicetasker.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model for Task.
 * Used throughout the app for business logic.
 */
@Serializable
data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: String,
    val updatedAt: String,
    val dueDate: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val voiceTranscription: String? = null,
    val aiExtractedIntent: String? = null
)

/**
 * Request model for creating a task.
 */
@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val priority: String = "MEDIUM",
    val reminderEnabled: Boolean = false,
    val reminderTime: String? = null,
    val voiceTranscription: String? = null
)

/**
 * Request model for updating a task.
 */
@Serializable
data class UpdateTaskRequest(
    val title: String? = null,
    val description: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val dueDate: String? = null,
    val reminderEnabled: Boolean? = null,
    val reminderTime: String? = null
)

/**
 * Task status enumeration.
 */
@Serializable
enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

/**
 * Task priority enumeration.
 */
@Serializable
enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}
