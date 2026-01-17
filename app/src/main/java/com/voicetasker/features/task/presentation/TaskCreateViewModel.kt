package com.voicetasker.features.task.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetasker.core.model.CreateTaskRequest
import com.voicetasker.core.model.TaskPriority
import com.voicetasker.features.task.data.repository.FakeTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Task Create screen.
 * Manages task creation with form inputs and validation.
 */
@HiltViewModel
class TaskCreateViewModel @Inject constructor(
    private val taskRepository: FakeTaskRepository
) : ViewModel() {

    // Form State
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val priority = mutableStateOf(TaskPriority.MEDIUM)
    val dueDate = mutableStateOf("")
    val reminderEnabled = mutableStateOf(false)
    val voiceInput = mutableStateOf("")

    // UI State
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val isCreateSuccess = mutableStateOf(false)

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
     * Creates a new task.
     */
    fun createTask() {
        if (!validateForm()) {
            return
        }

        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            val request = CreateTaskRequest(
                title = title.value,
                description = description.value.ifBlank { null },
                dueDate = dueDate.value.ifBlank { null },
                priority = priority.value,
                reminderEnabled = reminderEnabled.value,
                voiceTranscription = voiceInput.value.ifBlank { null }
            )

            val result = taskRepository.createTask(request = request)

            isLoading.value = false

            result.onSuccess { task ->
                isCreateSuccess.value = true
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to create task"
            }
        }
    }

    /**
     * Resets the form.
     */
    fun resetForm() {
        title.value = ""
        description.value = ""
        priority.value = TaskPriority.MEDIUM
        dueDate.value = ""
        reminderEnabled.value = false
        voiceInput.value = ""
        error.value = null
        isCreateSuccess.value = false
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
    fun resetCreateSuccess() {
        isCreateSuccess.value = false
    }
}
