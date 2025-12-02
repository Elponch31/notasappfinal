package com.example.notasapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasapp.Note
import com.example.notasapp.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> get() = _notes

    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val audioPath = MutableStateFlow<String?>(null)
    val currentNote = MutableStateFlow<Note?>(null)

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notes.value = repository.getAll()
        }
    }

    fun addNote() {
        viewModelScope.launch {
            repository.insert(
                Note(
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value
                )
            )
            loadNotes()
        }
    }

    fun updateNote(id: Int) {
        viewModelScope.launch {
            repository.update(
                Note(
                    id = id,
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value
                )
            )
            loadNotes()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
            loadNotes()
        }
    }

    fun loadNoteById(id: Int) {
        viewModelScope.launch {
            if (id != 0) {
                val note = repository.getById(id)
                note?.let {
                    currentNote.value = it
                    title.value = it.title
                    content.value = it.content
                    audioPath.value = it.audioPath
                }
            }
        }
    }


    fun clearNote() {
        title.value = ""
        content.value = ""
        audioPath.value = null
        currentNote.value = null
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
