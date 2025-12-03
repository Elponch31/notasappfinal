package com.example.notasapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.notasapp.media.MediaViewModel
import com.example.notasapp.media.PhotosScreen
import com.example.notasapp.ui.theme.TaskViewModel
import java.io.File
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTaskScreen(
    navController: NavController,
    vm: TaskViewModel,
    mediaVm: MediaViewModel,
    taskId: Int
) {
    val context = LocalContext.current
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()
    val audioRecorder = remember { AudioRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var lastAudioFile by remember { mutableStateOf<File?>(null) }

    // ------------------- Archivos adjuntos -------------------
    val attachedFiles = vm.attachedFiles
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.addAttachedFile(it) }
    }
    // ----------------------------------------------------------

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

    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startOrStopRecording()
        else Toast.makeText(context, "Permiso de micrófono denegado", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(taskId) {
        vm.loadTaskById(taskId)
        vm.audioPath.value?.let { path ->
            val file = File(path)
            if (file.exists()) lastAudioFile = file
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (taskId == 0) "Nueva tarea" else "Editar tarea",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        vm.audioPath.value = lastAudioFile?.absolutePath
                        if (taskId == 0) vm.addTask() else vm.updateTask(taskId)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                ) {
                    Text(
                        "Guardar tarea",
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelLarge.fontSize
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(8.dp)
        ) {
            // ---------------- Título ----------------
            OutlinedTextField(
                value = title,
                onValueChange = { vm.title.value = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- Contenido ----------------
            OutlinedTextField(
                value = content,
                onValueChange = { vm.content.value = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- Botón para adjuntar archivos ----------------
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Adjuntar archivo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- Lista de archivos adjuntos ----------------
            attachedFiles.forEach { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Abrir archivo al click
                    Text(
                        text = uri.lastPathSegment ?: "Archivo seleccionado",
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, context.contentResolver.getType(uri))
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(intent)
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón eliminar
                    IconButton(onClick = { vm.removeAttachedFile(uri) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar archivo"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- Fotos y videos ----------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                PhotosScreen(mediaVm)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- Grabación de audio ----------------
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Botón Grabar
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val permission = Manifest.permission.RECORD_AUDIO
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
                                startOrStopRecording()
                            else recordPermissionLauncher.launch(permission)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.7f)
                            .height(36.dp)
                    ) {
                        Text(
                            if (isRecording) "Detener" else "Grabar",
                            color = Color.White,
                            fontSize = MaterialTheme.typography.labelLarge.fontSize
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Reproducir
                lastAudioFile?.let { file ->
                    if (file.exists()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { playAudio() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth(0.7f)
                                    .height(36.dp)
                            ) {
                                Text(
                                    if (isPlaying) "⏹ Detener" else "▶ Reproducir",
                                    color = Color.White,
                                    fontSize = MaterialTheme.typography.labelLarge.fontSize
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
