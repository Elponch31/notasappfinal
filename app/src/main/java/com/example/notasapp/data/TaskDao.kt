package com.example.notasapp

import androidx.room.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}
