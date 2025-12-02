package com.example.notasapp

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(navController: NavController, vm: TaskViewModel, taskId: Int) {

    val context = LocalContext.current
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()
    val audioRecorder = remember { AudioRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var lastAudioFile by remember { mutableStateOf<File?>(null) }

    // ------------------ Funciones ------------------
    fun startOrStopRecording() {
        val outputFile = File(context.filesDir, "audio_${System.currentTimeMillis()}.m4a")
        if (!isRecording) {
            audioRecorder.startRecording(outputFile)
            lastAudioFile = outputFile
            isRecording = true
            Toast.makeText(context, "Grabación iniciada", Toast.LENGTH_SHORT).show()
        } else {
            audioRecorder.stopRecording()
            isRecording = false
            Toast.makeText(context, "Grabación guardada", Toast.LENGTH_SHORT).show()
        }
    }

    fun playAudio() {
        val file = lastAudioFile ?: return
        if (isPlaying) {
            audioRecorder.stopPlayback()
            isPlaying = false
        } else {
            audioRecorder.play(file) { isPlaying = false }
            isPlaying = true
        }
    }

    // ------------------ Permisos ------------------
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startOrStopRecording()
        else Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
    }

    // ------------------ Cargar tarea ------------------
    LaunchedEffect(taskId) {
        vm.loadTaskById(taskId)
        vm.audioPath.value?.let { path ->
            val file = File(path)
            if (file.exists()) lastAudioFile = file
        }
    }

    // ------------------ UI ------------------
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
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
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
                    onClick = {
                        val permission = Manifest.permission.RECORD_AUDIO
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                            startOrStopRecording()
                        else recordPermissionLauncher.launch(permission)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(if (isRecording) "Detener grabación" else "Grabar audio", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                lastAudioFile?.let { file ->
                    if (file.exists()) {
                        Button(
                            onClick = { playAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Text(if (isPlaying) "⏹ Detener Audio" else "▶️ Reproducir Audio", color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Button(
                    onClick = {
                        vm.audioPath.value = lastAudioFile?.absolutePath
                        if (taskId == 0) vm.addTask() else vm.updateTask(taskId)
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

