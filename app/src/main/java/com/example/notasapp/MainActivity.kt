package com.example.notasapp

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

        val database = NoteDatabase.getDatabase(this)
        val noteRepository = NoteRepository(database.noteDao())
        val taskRepository = TaskRepository(database.taskDao())

        setContent {
            NotasAppTheme {
                val navController: NavHostController = rememberNavController()

                val noteVm: NoteViewModel = viewModel(factory = NoteViewModelFactory(noteRepository))
                val taskVm: TaskViewModel = viewModel(factory = TaskViewModelFactory(taskRepository))
                val mediaVm: MediaViewModel = viewModel(factory = MediaViewModelFactory(application = application))

                NavHost(
                    navController = navController,
                    startDestination = "notes"
                ) {

                    composable("notes") {
                        NotesScreen(navController, noteVm, taskVm, mediaVm)
                    }

                    composable("new_note/{noteId}") { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
                        NewNoteScreen(navController, noteVm, noteId)
                    }

                    composable("new_task/{taskId}") { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull() ?: 0
                        NewTaskScreen(navController, taskVm, taskId)
                    }
                }
            }
        }
    }
}
