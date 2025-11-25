package com.example.notasapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.notasapp.ui.theme.TaskViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(navController: NavController, vm: TaskViewModel, taskId: Int) {

    val context = LocalContext.current
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()
    val currentTask by vm.currentTask.collectAsState()
    val audioPath by vm.audioPath.collectAsState()

    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var player: MediaPlayer? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(taskId) {
        vm.loadTaskById(taskId)
    }

    fun checkPermissionAndRecord() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {

            if (!isRecording) {
                startRecording(context) { rec, path ->
                    recorder = rec
                    vm.audioPath.value = path
                    isRecording = true
                }
            } else {
                stopRecording(recorder) {
                    recorder = null
                    isRecording = false
                    Toast.makeText(context, "Grabación guardada", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (taskId == 0) stringResource(R.string.new_task)
                        else stringResource(R.string.edit_task),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = {

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = { checkPermissionAndRecord() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(if (isRecording) "Detener grabación" else "Grabar audio", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ▶► REPRODUCIR
                if (audioPath != null) {
                    Button(
                        onClick = {
                            player?.stop()
                            player = MediaPlayer().apply {
                                setDataSource(audioPath)
                                prepare()
                                start()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Text("Reproducir audio", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (taskId != 0) {
                    Button(
                        onClick = {
                            currentTask?.let {
                                vm.deleteTask(it)
                                navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Text("Eliminar", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (taskId == 0) vm.addTask()
                        else vm.updateTask(taskId)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text("Guardar", color = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = title,
                onValueChange = { vm.title.value = it },
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { vm.content.value = it },
                label = { Text(stringResource(R.string.content)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
        }
    }
}
