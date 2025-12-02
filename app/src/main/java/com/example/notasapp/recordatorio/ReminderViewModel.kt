package com.example.notasapp.recordatorio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> get() = _reminders

    fun loadReminders(taskId: Int) {
        viewModelScope.launch {
            _reminders.value = repository.getByTask(taskId)
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insert(reminder)
            loadReminders(reminder.taskId)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
            loadReminders(reminder.taskId)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
            loadReminders(reminder.taskId)
        }
    }
}
