package com.example.notasapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object AlarmHelper {

    fun programReminder(context: Context, time: Long, title: String) {

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("titulo", title)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            time.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pending
        )
    }
}
