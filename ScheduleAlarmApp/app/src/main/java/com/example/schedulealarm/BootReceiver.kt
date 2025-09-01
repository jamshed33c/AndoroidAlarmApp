package com.example.schedulealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
            val trigger = prefs.getLong("triggerAt", -1L)
            if (trigger > 0) {
                val hour = prefs.getInt("hour", 8)
                val minute = prefs.getInt("minute", 0)
                val label = prefs.getString("label", "") ?: ""

                val cal = Calendar.getInstance().apply {
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
                }

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val i = Intent(context, AlarmReceiver::class.java).apply { putExtra("label", label) }
                val pending = PendingIntent.getBroadcast(
                    context, 1001, i,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val show = PendingIntent.getActivity(
                    context, 2001, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val info = AlarmManager.AlarmClockInfo(cal.timeInMillis, show)
                alarmManager.setAlarmClock(info, pending)
            }
        }
    }
}