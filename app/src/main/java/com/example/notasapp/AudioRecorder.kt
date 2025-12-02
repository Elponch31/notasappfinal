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

    /** Inicia la grabación de audio */
    fun startRecording(outputFile: File) {
        recorder?.release()
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
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

    /** Detiene la grabación de audio */
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
     * Reproduce un archivo de audio
     * @param file archivo a reproducir
     * @param onCompletion callback cuando termina la reproducción
     */
    fun play(file: File, onCompletion: () -> Unit) {
        if (!file.exists()) return
        player?.release()
        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    onCompletion()
                    release()
                    player = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al reproducir audio", e)
                release()
                player = null
            }
        }
    }

    /** Detiene solo la reproducción */
    fun stopPlayback() {
        player?.apply {
            if (isPlaying) stop()
            release()
        }
        player = null
    }

    /** Libera todos los recursos de grabación y reproducción */
    fun releaseAll() {
        recorder?.release()
        recorder = null
        player?.release()
        player = null
    }
}
