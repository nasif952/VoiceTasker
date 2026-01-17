package com.voicetasker.features.task.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetasker.core.model.TaskPriority
import com.voicetasker.core.model.UpdateTaskRequest
import com.voicetasker.features.task.data.repository.FakeTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Task Edit screen.
 * Manages task editing with form inputs and validation.
 */
@HiltViewModel
class TaskEditViewModel @Inject constructor(
    private val taskRepository: FakeTaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Task ID from navigation
    private val taskId: String = savedStateHandle.get<String>("taskId") ?: ""

    // Form State
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val priority = mutableStateOf(TaskPriority.MEDIUM)
    val dueDate = mutableStateOf("")

    // UI State
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isUpdateSuccess = mutableStateOf(false)
    val isTaskLoaded = mutableStateOf(false)

    init {
        loadTask()
    }

    /**
     * Load task data for editing.
     */
    private fun loadTask() {
        if (taskId.isBlank()) {
            error.value = "Task not found"
            return
        }

        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            val result = taskRepository.getTaskById(taskId)

            isLoading.value = false

            result.onSuccess { task ->
                title.value = task.title
                description.value = task.description
                priority.value = task.priority
                dueDate.value = task.dueDate?.substring(0, 10) ?: ""
                isTaskLoaded.value = true
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to load task"
            }
        }
    }

    /**
     * Validates task form.
     */
    private fun validateForm(): Boolean {
        error.value = null

        if (title.value.isBlank()) {
            error.value = "Task title is required"
            return false
        }

        if (title.value.length < 3) {
            error.value = "Task title must be at least 3 characters"
            return false
        }

        return true
    }

    /**
     * Updates the task.
     */
    fun updateTask() {
        if (!validateForm()) {
            return
        }

        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            val request = UpdateTaskRequest(
                title = title.value,
                description = description.value.ifBlank { null },
                dueDate = dueDate.value.ifBlank { null },
                priority = priority.value
            )

            val result = taskRepository.updateTask(taskId = taskId, request = request)

            isLoading.value = false

            result.onSuccess { task ->
                isUpdateSuccess.value = true
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to update task"
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Resets the success state (used after navigation).
     */
    fun resetUpdateSuccess() {
        isUpdateSuccess.value = false
    }
}
