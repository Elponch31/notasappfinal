package com.example.notasapp.media

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasapp.NoteDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MediaViewModel(application: Application, private val repository: MediaRepository) : AndroidViewModel(application) {

    // Exponemos la lista como StateFlow (similar a tus otros VMs)
    val mediaList = repository.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun insertUri(uriString: String) {
        viewModelScope.launch {
            repository.insert(MediaEntity(uri = uriString))
        }
    }

    fun delete(media: MediaEntity) {
        viewModelScope.launch {
            repository.delete(media)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}

// Factory para crear MediaViewModel desde Activity (igual a tus otros factories)
class MediaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            val db = NoteDatabase.getDatabase(application)
            val repo = MediaRepository(db.mediaDao())
            return MediaViewModel(application, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
