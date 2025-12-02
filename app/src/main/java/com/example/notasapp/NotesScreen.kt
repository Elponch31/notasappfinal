package com.example.notasapp

import android.Manifest
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.notasapp.media.MediaViewModel
import com.example.notasapp.media.PhotosScreen
import com.example.notasapp.ui.theme.NoteViewModel
import com.example.notasapp.ui.theme.TaskViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    noteVm: NoteViewModel,
    taskVm: TaskViewModel,
    mediaVm: MediaViewModel
) {
    var selectedSection by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.notes_and_tasks),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50)
                )
            )
        }
    ) { padding ->

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ---------------- PANEL LATERAL ----------------
            Column(
                modifier = Modifier
                    .width(75.dp)
                    .fillMaxHeight()
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Button(
                    onClick = { selectedSection = 0 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (selectedSection == 0) Color(0xFF81C784)
                            else Color(0xFFE0E0E0)
                    ),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Notas/Tareas", fontSize = 11.sp)
                }

                Button(
                    onClick = { selectedSection = 1 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (selectedSection == 1) Color(0xFF81C784)
                            else Color(0xFFE0E0E0)
                    ),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Fotos/Videos", fontSize = 11.sp)
                }
            }

            // ---------------- CONTENIDO DERECHA ----------------
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                when (selectedSection) {

                    // ---------------- NOTAS / TAREAS ----------------
                    0 -> {

                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

                            if (maxWidth < 800.dp) {
                                Column {
                                    val tabs = listOf(
                                        stringResource(R.string.notes),
                                        stringResource(R.string.tasks)
                                    )

                                    TabRow(selectedTabIndex = selectedTab) {
                                        tabs.forEachIndexed { index, title ->
                                            Tab(
                                                selected = selectedTab == index,
                                                onClick = { selectedTab = index },
                                                text = { Text(title) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (selectedTab == 0) NotesList(navController, noteVm)
                                    else TasksList(navController = navController, taskVm = taskVm)
                                }

                                FloatingActionButton(
                                    onClick = {
                                        if (selectedTab == 0) noteVm.clearNote()
                                        else taskVm.clearTask()
                                        if (selectedTab == 0)
                                            navController.navigate("new_note/0")
                                        else
                                            navController.navigate("new_task/0")
                                    },
                                    containerColor = Color(0xFF4CAF50),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    Icon(Icons.Default.Add, "Agregar", tint = Color.White)
                                }
                            } else {

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(32.dp)
                                ) {

                                    // Notas
                                    Box(modifier = Modifier.weight(1f)) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                text = stringResource(R.string.notes),
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            NotesList(navController, noteVm)
                                        }

                                        FloatingActionButton(
                                            onClick = {
                                                noteVm.clearNote()
                                                navController.navigate("new_note/0")
                                            },
                                            containerColor = Color(0xFF4CAF50),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, "Agregar nota", tint = Color.White)
                                        }
                                    }

                                    // Tareas
                                    Box(modifier = Modifier.weight(1f)) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            Text(
                                                text = stringResource(R.string.tasks),
                                                style = MaterialTheme.typography.titleLarge,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            TasksList(navController = navController, taskVm = taskVm)
                                        }

                                        FloatingActionButton(
                                            onClick = {
                                                taskVm.clearTask()
                                                navController.navigate("new_task/0")
                                            },
                                            containerColor = Color(0xFF4CAF50),
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, "Agregar tarea", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ---------------- FOTOS / VIDEOS ----------------
                    1 -> {
                        PhotosScreen(mediaVm)
                    }
                }
            }
        }
    }
}

// ----------------- LISTA DE NOTAS -----------------
@Composable
fun NotesList(navController: NavController, noteVm: NoteViewModel) {
    val notes by noteVm.notes.collectAsState()
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    var playingNoteId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate("new_note/${note.id}") }
                        ) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }
                    }

                    note.audioPath?.let { path ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val isPlaying = playingNoteId == note.id
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    audioRecorder.stopPlayback()
                                    playingNoteId = null
                                } else {
                                    audioRecorder.play(File(path)) { playingNoteId = null }
                                    playingNoteId = note.id
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text(if (isPlaying) "⏹ Detener Audio" else "▶️ Reproducir Audio", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ----------------- LISTA DE TAREAS -----------------
@Composable
fun TasksList(navController: NavController, taskVm: TaskViewModel) {
    val tasks by taskVm.tasks.collectAsState()
    val context = LocalContext.current
    val audioRecorder = remember { AudioRecorder(context) }
    var playingTaskId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks) { task ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate("new_task/${task.id}") }
                        ) {
                            Text(task.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        }

                        Checkbox(checked = false, onCheckedChange = { taskVm.deleteTask(task) })
                    }

                    task.audioPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val isPlaying = playingTaskId == task.id
                            Button(onClick = {
                                if (isPlaying) {
                                    audioRecorder.stopPlayback()
                                    playingTaskId = null
                                } else {
                                    audioRecorder.play(file) { playingTaskId = null }
                                    playingTaskId = task.id
                                }
                            }) {
                                Text(if (isPlaying) "⏹ Detener Audio" else "▶️ Reproducir Audio")
                            }
                        }
                    }
                }
            }
        }
    }
}
