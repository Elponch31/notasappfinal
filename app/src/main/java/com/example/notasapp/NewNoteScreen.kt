package com.example.notasapp

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.notasapp.ui.theme.NoteViewModel
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewNoteScreen(
    navController: NavController,
    vm: NoteViewModel,
    mediaVm: MediaViewModel,
    noteId: Int
) {
    val context = LocalContext.current
    val title by vm.title.collectAsState()
    val content by vm.content.collectAsState()
    val audioRecorder = remember { AudioRecorder(context) }

    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var lastAudioFile by remember { mutableStateOf<File?>(null) }

    // -----------------------------------------
    //      RECORDATORIOS (NUEVO)
    // -----------------------------------------
    val reminders = remember { mutableStateListOf<Long>() }

    // Si la nota ya existe, cargamos sus recordatorios
    LaunchedEffect(noteId) {
        vm.loadNoteById(noteId)
        vm.audioPath.value?.let { path ->
            val file = File(path)
            if (file.exists()) lastAudioFile = file
        }

        vm.remindersJson.value?.let { json ->
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                reminders.add(arr.getLong(i))
            }
        }
    }

    fun addReminder() {
        val now = Calendar.getInstance()

        DatePickerDialog(
            context,
            { _, y, m, d ->
                TimePickerDialog(
                    context,
                    { _, h, min ->
                        val cal = Calendar.getInstance()
                        cal.set(y, m, d, h, min, 0)

                        reminders.add(cal.timeInMillis)
                        Toast.makeText(context, "Recordatorio añadido", Toast.LENGTH_SHORT).show()

                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // -----------------------------------------
    //      AUDIO
    // -----------------------------------------
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (noteId == 0) "Nueva nota" else "Editar nota",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50))
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        vm.audioPath.value = lastAudioFile?.absolutePath

                        val json = JSONArray()
                        reminders.forEach { json.put(it) }
                        vm.remindersJson.value = json.toString()

                        if (noteId == 0) vm.addNote() else vm.updateNote(noteId)

                        reminders.forEach { time ->
                            AlarmHelper.programReminder(context, time, title)
                        }

                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(50.dp)
                ) {
                    Text("Guardar nota", color = Color.White)
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

            OutlinedTextField(
                value = content,
                onValueChange = { vm.content.value = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Fotos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                PhotosScreen(mediaVm)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------------- RECORDATORIOS (UI) ----------------
            Text("Recordatorios:", style = MaterialTheme.typography.titleMedium)

            reminders.forEach { r ->
                Text("⏰ " + SimpleDateFormat("dd/MM/yyyy HH:mm").format(r))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { addReminder() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir recordatorio", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grabación de audio
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        val permission = Manifest.permission.RECORD_AUDIO
                        if (ContextCompat.checkSelfPermission(context, permission) ==
                            PackageManager.PERMISSION_GRANTED
                        ) startOrStopRecording()
                        else recordPermissionLauncher.launch(permission)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088FF)),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(36.dp)
                ) {
                    Text(if (isRecording) "Detener" else "Grabar", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                lastAudioFile?.let { file ->
                    if (file.exists()) {
                        Button(
                            onClick = { playAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A)),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(36.dp)
                        ) {
                            Text(
                                if (isPlaying) "⏹ Detener" else "▶ Reproducir",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
