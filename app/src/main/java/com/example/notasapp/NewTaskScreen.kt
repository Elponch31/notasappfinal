package com.example.notasapp

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
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
import java.util.*
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
    val reminders by vm.reminders.collectAsState()

    val audioRecorder = remember { AudioRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var lastAudioFile by remember { mutableStateOf<File?>(null) }

    val attachedFiles = vm.attachedFiles
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.addAttachedFile(it) }
    }

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


    fun programarAlarma(timestamp: Long, titulo: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", titulo)
            putExtra("taskId", taskId)   // 👈🔥 AGREGADO
        }

        val pending = PendingIntent.getBroadcast(
            context,
            timestamp.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timestamp,
            pending
        )
    }


    fun agregarRecordatorio() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute, 0)
                        val millis = calendar.timeInMillis

                        vm.addReminder(millis)

                        Toast.makeText(context, "Recordatorio agregado", Toast.LENGTH_SHORT).show()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

                        reminders.forEach { time ->
                            programarAlarma(time, title)
                        }

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { agregarRecordatorio() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir recordatorio ⏰")
            }

            Spacer(modifier = Modifier.height(10.dp))


            reminders.forEach { timestamp ->
                val fecha = Date(timestamp).toString()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = fecha, modifier = Modifier.weight(1f))

                    IconButton(onClick = { vm.removeReminder(timestamp) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Adjuntar archivo")
            }

            attachedFiles.forEach { uri ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uri.lastPathSegment ?: "Archivo",
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

                    IconButton(onClick = { vm.removeAttachedFile(uri) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                PhotosScreen(mediaVm)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------- AUDIO ----------------
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Button(
                    onClick = {
                        val permission = Manifest.permission.RECORD_AUDIO
                        if (ContextCompat.checkSelfPermission(context, permission)
                            == PackageManager.PERMISSION_GRANTED
                        ) startOrStopRecording()
                        else recordPermissionLauncher.launch(permission)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(if (isRecording) "Detener" else "Grabar", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                lastAudioFile?.let { file ->
                    if (file.exists()) {
                        Button(
                            onClick = { playAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            modifier = Modifier.fillMaxWidth(0.7f)
                        ) {
                            Text(if (isPlaying) "Detener" else "Reproducir", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
