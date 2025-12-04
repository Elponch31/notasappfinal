package com.example.notasapp.ui.theme

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasapp.Task
import com.example.notasapp.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    val title = MutableStateFlow("")
    val content = MutableStateFlow("")
    val audioPath = MutableStateFlow<String?>(null)
    val currentTask = MutableStateFlow<Task?>(null)

    val attachedFiles = mutableStateListOf<Uri>()

    private val _reminders = MutableStateFlow<List<Long>>(emptyList())
    val reminders: StateFlow<List<Long>> get() = _reminders

    fun addReminder(time: Long) {
        _reminders.value = _reminders.value + time
    }

    fun removeReminder(time: Long) {
        _reminders.value = _reminders.value.filter { it != time }
    }

    fun clearReminders() {
        _reminders.value = emptyList()
    }

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

            val remindersJson = JSONArray().apply {
                _reminders.value.forEach { put(it) }
            }.toString()

            repository.insert(
                Task(
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value,
                    reminders = remindersJson
                )
            )
            loadTasks()
        }
    }

    fun updateTask(id: Int) {
        viewModelScope.launch {

            val remindersJson = JSONArray().apply {
                _reminders.value.forEach { put(it) }
            }.toString()

            repository.update(
                Task(
                    id = id,
                    title = title.value,
                    content = content.value,
                    audioPath = audioPath.value,
                    reminders = remindersJson
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

                    clearReminders()
                    it.reminders?.let { json ->
                        val arr = JSONArray(json)
                        val list = mutableListOf<Long>()
                        for (i in 0 until arr.length()) {
                            list.add(arr.getLong(i))
                        }
                        _reminders.value = list
                    }

                    attachedFiles.clear()
                }
            }
        }
    }

    fun clearTask() {
        title.value = ""
        content.value = ""
        audioPath.value = null
        currentTask.value = null

        attachedFiles.clear()
        clearReminders()
    }

    fun addAttachedFile(uri: Uri) {
        attachedFiles.add(uri)
    }

    fun removeAttachedFile(uri: Uri) {
        attachedFiles.remove(uri)
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
