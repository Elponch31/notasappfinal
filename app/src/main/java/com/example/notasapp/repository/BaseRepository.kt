package com.example.notasapp


abstract class BaseRepository<T> {

    abstract suspend fun getAll(): List<T>

    abstract suspend fun getById(id: Int): T?

    abstract suspend fun insert(item: T)

    abstract suspend fun update(item: T)

    abstract suspend fun delete(item: T)
}