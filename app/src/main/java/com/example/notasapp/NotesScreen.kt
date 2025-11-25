package com.example.notasapp

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.notasapp.ui.theme.NoteViewModel
import com.example.notasapp.ui.theme.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    noteVm: NoteViewModel,
    taskVm: TaskViewModel
) {
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

        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {

            // ——————————————————————————
            //  PANTALLA PEQUEÑA (MÓVILES)
            // ——————————————————————————
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

                    if (selectedTab == 0) {
                        NotesList(navController, noteVm)
                    } else {
                        TasksList(navController, taskVm)
                    }
                }

                FloatingActionButton(
                    onClick = {
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
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
                }
            }

            // ——————————————————————————
            //  PANTALLA GRANDE (TABLET)
            // ——————————————————————————
            else {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {

                    // ——— NOTAS ———
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
                            onClick = { navController.navigate("new_note/0") },
                            containerColor = Color(0xFF4CAF50),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar nota", tint = Color.White)
                        }
                    }

                    // ——— TAREAS ———
                    Box(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.fillMaxSize()) {

                            Text(
                                text = stringResource(R.string.tasks),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            TasksList(navController, taskVm)
                        }

                        FloatingActionButton(
                            onClick = { navController.navigate("new_task/0") },
                            containerColor = Color(0xFF4CAF50),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar tarea", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesList(navController: NavController, noteVm: NoteViewModel) {
    val notes by noteVm.notes.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(notes) { note ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { navController.navigate("new_note/${note.id}") },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun TasksList(navController: NavController, taskVm: TaskViewModel) {
    val tasks by taskVm.tasks.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tasks) { task ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("new_task/${task.id}") }
                    ) {
                        Text(task.title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            task.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Checkbox(
                        checked = false,
                        onCheckedChange = { taskVm.deleteTask(task) }
                    )
                }
            }
        }
    }
}
