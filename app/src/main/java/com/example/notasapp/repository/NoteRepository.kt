package com.example.notasapp

class NoteRepository(private val dao: NoteDao) : BaseRepository<Note>() {

    override suspend fun getAll(): List<Note> = dao.getAllNotes()

    override suspend fun getById(id: Int): Note? = dao.getNoteById(id)

    override suspend fun insert(item: Note) = dao.insert(item)

    override suspend fun update(item: Note) = dao.update(item)

    override suspend fun delete(item: Note) = dao.delete(item)
}