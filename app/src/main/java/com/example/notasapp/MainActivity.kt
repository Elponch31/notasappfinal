package com.example.notasapp

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notasapp.media.MediaViewModel
import com.example.notasapp.media.MediaViewModelFactory
import com.example.notasapp.ui.theme.NoteViewModel
import com.example.notasapp.ui.theme.NoteViewModelFactory
import com.example.notasapp.ui.theme.TaskViewModel
import com.example.notasapp.ui.theme.TaskViewModelFactory
import com.example.notasapp.ui.theme.NotasAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crearCanalNotificaciones()

        val database = NoteDatabase.getDatabase(this)
        val noteRepository = NoteRepository(database.noteDao())
        val taskRepository = TaskRepository(database.taskDao())


        val taskIdDesdeNotificacion = intent.getIntExtra("taskId", 0)

        setContent {
            NotasAppTheme {

                val navController: NavHostController = rememberNavController()

                val noteVm: NoteViewModel =
                    viewModel(factory = NoteViewModelFactory(noteRepository))

                val taskVm: TaskViewModel =
                    viewModel(factory = TaskViewModelFactory(taskRepository))

                val mediaVm: MediaViewModel =
                    viewModel(factory = MediaViewModelFactory(application))

                androidx.compose.runtime.LaunchedEffect(taskIdDesdeNotificacion) {
                    if (taskIdDesdeNotificacion != 0) {
                        navController.navigate("new_task/$taskIdDesdeNotificacion")
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "notes"
                ) {
                    composable("notes") {
                        NotesScreen(navController, noteVm, taskVm, mediaVm)
                    }

                    composable("new_note/{noteId}") { backStackEntry ->
                        val noteId =
                            backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
                        NewNoteScreen(navController, noteVm, mediaVm, noteId)
                    }

                    composable("new_task/{taskId}") { backStackEntry ->
                        val taskId =
                            backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
                        NewTaskScreen(navController, taskVm, mediaVm, taskId)
                    }
                }
            }
        }
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recordatorios_ch",
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Canal para recordatorios de notas"

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun programarRecordatorio(titulo: String, tiempoEnMilis: Long) {
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("titulo", titulo)
        }

        val pending = PendingIntent.getBroadcast(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tiempoEnMilis,
            pending
        )
    }
}
