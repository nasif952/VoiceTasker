package com.voicetasker.features.task.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicetasker.core.model.Task
import com.voicetasker.core.model.TaskStatus
import com.voicetasker.core.model.TaskPriority
import com.voicetasker.features.task.data.repository.FakeTaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Task List (Home) screen.
 * Manages task listing, filtering, and deletion.
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: FakeTaskRepository
) : ViewModel() {

    // UI State
    val tasks = mutableStateOf<List<Task>>(emptyList())
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val selectedFilter = mutableStateOf<TaskStatus?>(null)

    init {
        loadTasks()
    }

    /**
     * Load all tasks from repository.
     */
    fun loadTasks() {
        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            val result = taskRepository.getTasks(status = selectedFilter.value)

            isLoading.value = false

            result.onSuccess { taskList ->
                tasks.value = taskList
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to load tasks"
            }
        }
    }

    /**
     * Filter tasks by status.
     */
    fun filterByStatus(status: TaskStatus?) {
        selectedFilter.value = status
        loadTasks()
    }

    /**
     * Delete a task.
     */
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = taskRepository.deleteTask(taskId)

            result.onSuccess {
                // Remove from UI
                tasks.value = tasks.value.filter { it.id != taskId }
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to delete task"
            }
        }
    }

    /**
     * Mark a task as completed.
     */
    fun completeTask(taskId: String) {
        viewModelScope.launch {
            val result = taskRepository.completeTask(taskId)

            result.onSuccess { updatedTask ->
                // Update in UI
                tasks.value = tasks.value.map {
                    if (it.id == taskId) updatedTask else it
                }
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to complete task"
            }
        }
    }

    /**
     * Toggle task completion status (complete/uncomplete).
     */
    fun toggleTaskStatus(taskId: String) {
        viewModelScope.launch {
            val result = taskRepository.toggleTaskStatus(taskId)

            result.onSuccess { updatedTask ->
                // Update in UI
                tasks.value = tasks.value.map {
                    if (it.id == taskId) updatedTask else it
                }
            }

            result.onFailure { exception ->
                error.value = exception.message ?: "Failed to update task"
            }
        }
    }

    /**
     * Refresh task list.
     */
    fun refreshTasks() {
        loadTasks()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Get task count by status.
     */
    fun getTaskCount(status: TaskStatus? = null): Int {
        return tasks.value.count { task ->
            status == null || task.status == status
        }
    }
}
