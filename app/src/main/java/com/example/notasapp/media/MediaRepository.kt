package com.example.notasapp.media

class MediaRepository(private val dao: MediaDao) {

    fun getAll() = dao.getAll()

    suspend fun insert(entity: MediaEntity): Long = dao.insert(entity)

    suspend fun delete(entity: MediaEntity) = dao.delete(entity)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}