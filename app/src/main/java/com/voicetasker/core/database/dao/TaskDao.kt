package com.voicetasker.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.voicetasker.core.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task entity.
 * Provides database operations for tasks.
 */
@Dao
interface TaskDao {
    @Insert
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    fun getTasksByUserId(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND status = :status AND is_deleted = 0 ORDER BY created_at DESC")
    fun getTasksByUserIdAndStatus(userId: String, status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND sync_status = :syncStatus AND is_deleted = 0")
    suspend fun getTasksBySyncStatus(userId: String, syncStatus: String): List<TaskEntity>

    @Query("UPDATE tasks SET sync_status = :syncStatus WHERE id = :taskId")
    suspend fun updateTaskSyncStatus(taskId: String, syncStatus: String)

    @Query("DELETE FROM tasks WHERE user_id = :userId AND is_deleted = 1")
    suspend fun deleteMarkedTasks(userId: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE user_id = :userId AND is_deleted = 0")
    fun getTaskCount(userId: String): Flow<Int>
}
