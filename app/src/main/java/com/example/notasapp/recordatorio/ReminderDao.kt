package com.example.notasapp.recordatorio

import androidx.room.*

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY timestamp ASC")
    suspend fun getRemindersByTask(taskId: Int): List<Reminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder)

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteByTask(taskId: Int)
}
