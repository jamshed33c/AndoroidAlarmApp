package com.example.schedulealarm

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: ""

        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("label", label)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 3001, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val contentIntent = PendingIntent.getActivity(
            context, 3002, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Alarm")
            .setContentText(if (label.isEmpty()) "Time's up!" else label)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        with(NotificationManagerCompat.from(context)) {
            notify(999, builder.build())
        }

        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("repeat", false)) {
            val hour = prefs.getInt("hour", 8)
            val minute = prefs.getInt("minute", 0)

            val cal = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                add(Calendar.DAY_OF_YEAR, 1)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(context, AlarmReceiver::class.java).apply { putExtra("label", label) }
            val pending = PendingIntent.getBroadcast(
                context, 1001, i,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            val showIntent = PendingIntent.getActivity(
                context, 2001, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            val info = AlarmManager.AlarmClockInfo(cal.timeInMillis, showIntent)
            alarmManager.setAlarmClock(info, pending)
            prefs.edit().putLong("triggerAt", cal.timeInMillis).apply()
        } else {
            prefs.edit().remove("triggerAt").apply()
        }
    }
}