package com.example.notasapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val titulo = intent.getStringExtra("title") ?: "Recordatorio"

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(context, "recordatorios_ch")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Recordatorio")
            .setContentText(titulo)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
