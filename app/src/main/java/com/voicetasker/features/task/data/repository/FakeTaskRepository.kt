package com.voicetasker.features.task.data.repository

import com.voicetasker.core.model.Task
import com.voicetasker.core.model.CreateTaskRequest
import com.voicetasker.core.model.UpdateTaskRequest
import com.voicetasker.core.model.TaskStatus
import com.voicetasker.core.model.TaskPriority
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID

/**
 * Fake implementation of TaskRepository for testing without a real backend.
 * Simulates task CRUD operations with in-memory storage.
 *
 * In production, this will be replaced with the real TaskRepository that calls actual APIs.
 */
class FakeTaskRepository {

    // Simulated database of tasks (in-memory)
    private val tasks = mutableMapOf(
        "task_1" to Task(
            id = "task_1",
            title = "Welcome to VoiceTasker",
            description = "This is your first task. Tap to edit or swipe to delete.",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            createdAt = LocalDateTime.now().minusHours(2).toString(),
            updatedAt = LocalDateTime.now().minusHours(2).toString(),
            dueDate = LocalDateTime.now().plusDays(1).toString(),
            reminderEnabled = false,
            reminderTime = null,
            voiceTranscription = null,
            aiExtractedIntent = null,
            syncStatus = "SYNCED"
        ),
        "task_2" to Task(
            id = "task_2",
            title = "Create a new task",
            description = "Use the + button to create a new task",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdAt = LocalDateTime.now().minusHours(1).toString(),
            updatedAt = LocalDateTime.now().minusHours(1).toString(),
            dueDate = LocalDateTime.now().plusDays(2).toString(),
            reminderEnabled = false,
            reminderTime = null,
            voiceTranscription = null,
            aiExtractedIntent = null,
            syncStatus = "SYNCED"
        )
    )

    /**
     * Get all tasks for a user.
     * Can be filtered by status, priority, or date.
     */
    suspend fun getTasks(
        userId: String = "current_user",
        status: TaskStatus? = null,
        priority: TaskPriority? = null
    ): Result<List<Task>> {
        delay(500) // Simulate network delay

        val filtered = tasks.values
            .filter { task ->
                (status == null || task.status == status) &&
                (priority == null || task.priority == priority)
            }
            .sortedByDescending { it.createdAt }

        return Result.success(filtered)
    }

    /**
     * Get a single task by ID.
     */
    suspend fun getTaskById(taskId: String): Result<Task> {
        delay(300)

        val task = tasks[taskId]
        return if (task != null) {
            Result.success(task)
        } else {
            Result.failure(Exception("Task not found"))
        }
    }

    /**
     * Create a new task.
     */
    suspend fun createTask(
        userId: String = "current_user",
        request: CreateTaskRequest
    ): Result<Task> {
        delay(800) // Simulate network delay

        // Validate request
        if (request.title.isBlank()) {
            return Result.failure(Exception("Task title cannot be empty"))
        }

        // Create task
        val newTask = Task(
            id = "task_${UUID.randomUUID()}",
            title = request.title,
            description = request.description ?: "",
            status = TaskStatus.TODO,
            priority = request.priority ?: TaskPriority.MEDIUM,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            dueDate = request.dueDate,
            reminderEnabled = request.reminderEnabled ?: false,
            reminderTime = request.reminderTime,
            voiceTranscription = request.voiceTranscription,
            aiExtractedIntent = request.aiExtractedIntent,
            syncStatus = "SYNCED"
        )

        tasks[newTask.id] = newTask
        return Result.success(newTask)
    }

    /**
     * Update an existing task.
     */
    suspend fun updateTask(
        taskId: String,
        request: UpdateTaskRequest
    ): Result<Task> {
        delay(800)

        val existingTask = tasks[taskId]
            ?: return Result.failure(Exception("Task not found"))

        // Update only provided fields
        val updatedTask = existingTask.copy(
            title = request.title ?: existingTask.title,
            description = request.description ?: existingTask.description,
            status = request.status ?: existingTask.status,
            priority = request.priority ?: existingTask.priority,
            dueDate = request.dueDate ?: existingTask.dueDate,
            reminderEnabled = request.reminderEnabled ?: existingTask.reminderEnabled,
            reminderTime = request.reminderTime ?: existingTask.reminderTime,
            updatedAt = LocalDateTime.now().toString(),
            syncStatus = "SYNCED"
        )

        tasks[taskId] = updatedTask
        return Result.success(updatedTask)
    }

    /**
     * Delete a task.
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        delay(500)

        if (!tasks.containsKey(taskId)) {
            return Result.failure(Exception("Task not found"))
        }

        tasks.remove(taskId)
        return Result.success(Unit)
    }

    /**
     * Mark a task as complete.
     */
    suspend fun completeTask(taskId: String): Result<Task> {
        delay(500)

        val task = tasks[taskId]
            ?: return Result.failure(Exception("Task not found"))

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            updatedAt = LocalDateTime.now().toString()
        )

        tasks[taskId] = completedTask
        return Result.success(completedTask)
    }

    /**
     * Helper to get all tasks (for testing).
     */
    fun getAllTasks(): List<Task> = tasks.values.toList()

    /**
     * Helper to reset tasks to default (for testing).
     */
    fun resetTasks() {
        tasks.clear()
        tasks["task_1"] = Task(
            id = "task_1",
            title = "Welcome to VoiceTasker",
            description = "This is your first task. Tap to edit or swipe to delete.",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
            createdAt = LocalDateTime.now().minusHours(2).toString(),
            updatedAt = LocalDateTime.now().minusHours(2).toString(),
            dueDate = LocalDateTime.now().plusDays(1).toString(),
            reminderEnabled = false,
            reminderTime = null,
            voiceTranscription = null,
            aiExtractedIntent = null,
            syncStatus = "SYNCED"
        )
    }
}
