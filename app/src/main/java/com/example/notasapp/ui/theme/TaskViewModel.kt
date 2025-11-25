package com.example.notasapp.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasapp.Task
import com.example.notasapp.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val audioPath = MutableStateFlow<String?>(null)
    val currentTask = MutableStateFlow<Task?>(null)

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getAll()
        }
    }

    fun addTask() {
        viewModelScope.launch {
            repository.insert(
                Task(
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value
                )
            )
            loadTasks()
        }
    }

    fun updateTask(id: Int) {
        viewModelScope.launch {
            repository.update(
                Task(
                    id = id,
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value
                )
            )
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            loadTasks()
        }
    }

    fun loadTaskById(id: Int) {
        viewModelScope.launch {
            if (id != 0) {
                val task = repository.getById(id)
                task?.let {
                    currentTask.value = it
                    title.value = it.title
                    content.value = it.content
                    audioPath.value = it.audioPath
                }
            }
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
