package com.example.notasapp

class TaskRepository(private val dao: TaskDao) : BaseRepository<Task>() {

    override suspend fun getAll(): List<Task> = dao.getAllTasks()

    override suspend fun getById(id: Int): Task? = dao.getTaskById(id)

    override suspend fun insert(item: Task) = dao.insert(item)

    override suspend fun update(item: Task) = dao.update(item)

    override suspend fun delete(item: Task) = dao.delete(item)
}
