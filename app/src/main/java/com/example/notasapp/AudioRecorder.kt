package com.example.notasapp

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private val TAG = "AudioRecorder"

    /**
     * Inicia la grabación de audio de forma segura.
     */
    fun startRecording(outputFile: File) {
        recorder?.release()

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // Formato moderno para mejor calidad y compatibilidad
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "Error de I/O al preparar la grabación", e)
                release()
                recorder = null
            } catch (e: IllegalStateException) {
                Log.e(TAG, "IllegalStateException al iniciar la grabación", e)
                release()
                recorder = null
            }
        }
    }

    /**
     * Detiene la grabación y libera el MediaRecorder.
     */
    fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: RuntimeException) {
                Log.e(TAG, "Error al detener la grabación (Runtime):", e)
                release()
            }
        }
        recorder = null
    }

    /**
     * Inicia la reproducción de un archivo de audio, asegurando la limpieza.
     */
    fun play(file: File) {
        player?.release()
        player = null

        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()

                // CRÍTICO: Liberar el recurso al finalizar la reproducción
                setOnCompletionListener { mp ->
                    mp.release()
                    player = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al reproducir audio", e)
                release()
                player = null
            }
        }
    }

    /**
     * Detiene y libera todos los recursos. ESENCIAL para el cleanup.
     */
    fun releaseAll() {
        recorder?.release()
        recorder = null

        player?.release()
        player = null
    }
}