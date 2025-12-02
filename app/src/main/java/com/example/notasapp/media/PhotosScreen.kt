package com.example.notasapp.media

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import android.Manifest

// ------------------ FUNCIÓN PARA CARGAR MINIATURAS ------------------
fun loadThumbnail(context: Context, uri: Uri, isVideo: Boolean): Bitmap? {
    return try {
        if (isVideo) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(120, 120), null)
            } else {
                ThumbnailUtils.createVideoThumbnail(
                    uri.path ?: "",
                    MediaStore.Images.Thumbnails.MINI_KIND
                )
            }
        } else {
            val input = context.contentResolver.openInputStream(uri)
            input?.use {
                val bmp = android.graphics.BitmapFactory.decodeStream(it)
                Bitmap.createScaledBitmap(bmp, 120, 120, true)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// ------------------ PHOTOS SCREEN ------------------
@Composable
fun PhotosScreen(mediaVm: MediaViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mediaList by mediaVm.mediaList.collectAsState(initial = emptyList())

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }

    // ------------------ LAUNCHERS ------------------
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) pendingPhotoUri?.let { mediaVm.insertUri(it.toString()) }
        pendingPhotoUri = null
    }

    val takeVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) pendingVideoUri?.let { mediaVm.insertUri(it.toString()) }
        pendingVideoUri = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val cameraGranted = result[Manifest.permission.CAMERA] ?: false
        val audioGranted = result[Manifest.permission.RECORD_AUDIO] ?: true // opcional si no grabas audio
        val readImagesGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            result[Manifest.permission.READ_MEDIA_IMAGES] ?: false else true
        val readVideosGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            result[Manifest.permission.READ_MEDIA_VIDEO] ?: false else true

        if (cameraGranted && audioGranted && readImagesGranted && readVideosGranted) {
            pendingPhotoUri?.let { takePictureLauncher.launch(it) }
            pendingVideoUri?.let { takeVideoLauncher.launch(it) }
        }
    }

    // ------------------ CREAR URI ------------------
    fun createImageUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NotasApp")
            }
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun createVideoUri(): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.DISPLAY_NAME, "VID_${System.currentTimeMillis()}.mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NotasApp")
            }
        }
        return context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }

    // ------------------ SOLICITAR PERMISOS ------------------
    fun requestPermissions() {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // ------------------ UI ------------------
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Fotos y Videos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(mediaList) { media ->
                MediaRow(media = media, onDelete = { m ->
                    coroutineScope.launch {
                        mediaVm.delete(m)
                        try { context.contentResolver.delete(Uri.parse(m.uri), null, null) }
                        catch (_: Exception) {}
                    }
                })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            // ---------------- FOTO ----------------
            FloatingActionButton(onClick = {
                pendingPhotoUri = createImageUri()
                requestPermissions()
            }) { Text("+ Foto", fontSize = 12.sp) }

            // ---------------- VIDEO ----------------
            FloatingActionButton(onClick = {
                pendingVideoUri = createVideoUri()
                requestPermissions()
            }) { Text("+ Video", fontSize = 12.sp) }
        }
    }
}

// ------------------ MEDIA ROW ------------------
@Composable
fun MediaRow(media: MediaEntity, onDelete: (MediaEntity) -> Unit) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(media.uri) {
        val uri = Uri.parse(media.uri)
        bitmap = loadThumbnail(context, uri, media.type == "video")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null) {
                Image(bitmap = bitmap!!.asImageBitmap(), contentDescription = null, modifier = Modifier.size(84.dp))
            } else {
                Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
                    Text("Cargando")
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(if (media.type == "video") "Video" else "Foto")

            }

            Button(onClick = { onDelete(media) }) { Text("Eliminar") }
        }
    }
}
