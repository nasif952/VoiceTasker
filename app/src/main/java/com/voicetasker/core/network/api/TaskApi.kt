package com.voicetasker.core.network.api

import com.voicetasker.core.model.CreateTaskRequest
import com.voicetasker.core.model.Task
import com.voicetasker.core.model.UpdateTaskRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit API interface for task endpoints.
 * TODO: Update base URL to point to actual Supabase backend
 */
interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(): List<Task>

    @GET("tasks/{taskId}")
    suspend fun getTask(@Path("taskId") taskId: String): Task

    @POST("tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Task

    @PUT("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskRequest
    ): Task

    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String)

    @GET("tasks/filter")
    suspend fun getTasksByStatus(
        @retrofit2.http.Query("status") status: String
    ): List<Task>
}
