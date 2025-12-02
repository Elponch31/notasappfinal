package com.example.notasapp.recordatorio

class ReminderRepository(private val dao: ReminderDao) {

    suspend fun getByTask(taskId: Int) = dao.getRemindersByTask(taskId)

    suspend fun insert(reminder: Reminder) = dao.insert(reminder)

    suspend fun update(reminder: Reminder) = dao.update(reminder)

    suspend fun delete(reminder: Reminder) = dao.delete(reminder)

    suspend fun deleteByTask(taskId: Int) = dao.deleteByTask(taskId)
}
