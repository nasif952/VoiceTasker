package com.voicetasker.features.task.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.voicetasker.core.model.CreateTaskRequest
import com.voicetasker.core.model.Task
import com.voicetasker.core.model.TaskPriority
import com.voicetasker.core.model.TaskStatus
import com.voicetasker.core.model.UpdateTaskRequest
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of TaskRepository.
 * Uses Firestore for cloud storage of tasks.
 */
@Singleton
class FirebaseTaskRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) {
    private val tasksCollection = firestore.collection("tasks")

    private val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")

    /**
     * Get all tasks for current user.
     */
    suspend fun getTasks(
        status: TaskStatus? = null,
        priority: TaskPriority? = null
    ): Result<List<Task>> {
        return try {
            var query = tasksCollection
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)

            val snapshot = query.get().await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                doc.toTask()
            }.filter { task ->
                (status == null || task.status == status) &&
                (priority == null || task.priority == priority)
            }

            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load tasks: ${e.message}"))
        }
    }

    /**
     * Get a single task by ID.
     */
    suspend fun getTaskById(taskId: String): Result<Task> {
        return try {
            val doc = tasksCollection.document(taskId).get().await()
            val task = doc.toTask()
            if (task != null && task.userId == currentUserId) {
                Result.success(task)
            } else {
                Result.failure(Exception("Task not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load task: ${e.message}"))
        }
    }

    /**
     * Create a new task.
     */
    suspend fun createTask(request: CreateTaskRequest): Result<Task> {
        return try {
            val taskId = UUID.randomUUID().toString()
            val now = LocalDateTime.now().toString()

            val task = Task(
                id = taskId,
                title = request.title,
                description = request.description ?: "",
                status = TaskStatus.TODO,
                priority = request.priority ?: TaskPriority.MEDIUM,
                createdAt = now,
                updatedAt = now,
                dueDate = request.dueDate,
                reminderEnabled = request.reminderEnabled ?: false,
                reminderTime = request.reminderTime,
                voiceTranscription = request.voiceTranscription,
                aiExtractedIntent = request.aiExtractedIntent,
                syncStatus = "SYNCED",
                userId = currentUserId
            )

            tasksCollection.document(taskId).set(task.toMap()).await()
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create task: ${e.message}"))
        }
    }

    /**
     * Update an existing task.
     */
    suspend fun updateTask(taskId: String, request: UpdateTaskRequest): Result<Task> {
        return try {
            val existingResult = getTaskById(taskId)
            if (existingResult.isFailure) {
                return existingResult
            }

            val existing = existingResult.getOrThrow()
            val now = LocalDateTime.now().toString()

            val updated = existing.copy(
                title = request.title ?: existing.title,
                description = request.description ?: existing.description,
                status = request.status ?: existing.status,
                priority = request.priority ?: existing.priority,
                dueDate = request.dueDate ?: existing.dueDate,
                reminderEnabled = request.reminderEnabled ?: existing.reminderEnabled,
                reminderTime = request.reminderTime ?: existing.reminderTime,
                updatedAt = now
            )

            tasksCollection.document(taskId).set(updated.toMap()).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update task: ${e.message}"))
        }
    }

    /**
     * Delete a task.
     */
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            tasksCollection.document(taskId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete task: ${e.message}"))
        }
    }

    /**
     * Toggle task completion status.
     */
    suspend fun toggleTaskStatus(taskId: String): Result<Task> {
        return try {
            val existingResult = getTaskById(taskId)
            if (existingResult.isFailure) {
                return existingResult
            }

            val existing = existingResult.getOrThrow()
            val newStatus = if (existing.status == TaskStatus.COMPLETED) {
                TaskStatus.TODO
            } else {
                TaskStatus.COMPLETED
            }

            val updated = existing.copy(
                status = newStatus,
                updatedAt = LocalDateTime.now().toString()
            )

            tasksCollection.document(taskId).set(updated.toMap()).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update task: ${e.message}"))
        }
    }

    /**
     * Mark task as completed.
     */
    suspend fun completeTask(taskId: String): Result<Task> {
        return try {
            val existingResult = getTaskById(taskId)
            if (existingResult.isFailure) {
                return existingResult
            }

            val existing = existingResult.getOrThrow()
            val updated = existing.copy(
                status = TaskStatus.COMPLETED,
                updatedAt = LocalDateTime.now().toString()
            )

            tasksCollection.document(taskId).set(updated.toMap()).await()
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to complete task: ${e.message}"))
        }
    }

    /**
     * Convert Firestore document to Task.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toTask(): Task? {
        return try {
            Task(
                id = id,
                title = getString("title") ?: return null,
                description = getString("description") ?: "",
                status = TaskStatus.valueOf(getString("status") ?: "TODO"),
                priority = TaskPriority.valueOf(getString("priority") ?: "MEDIUM"),
                createdAt = getString("createdAt") ?: "",
                updatedAt = getString("updatedAt") ?: "",
                dueDate = getString("dueDate"),
                reminderEnabled = getBoolean("reminderEnabled") ?: false,
                reminderTime = getString("reminderTime"),
                voiceTranscription = getString("voiceTranscription"),
                aiExtractedIntent = getString("aiExtractedIntent"),
                syncStatus = getString("syncStatus") ?: "SYNCED",
                userId = getString("userId") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert Task to Firestore map.
     */
    private fun Task.toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "status" to status.name,
            "priority" to priority.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "dueDate" to dueDate,
            "reminderEnabled" to reminderEnabled,
            "reminderTime" to reminderTime,
            "voiceTranscription" to voiceTranscription,
            "aiExtractedIntent" to aiExtractedIntent,
            "syncStatus" to syncStatus,
            "userId" to userId
        )
    }
}
