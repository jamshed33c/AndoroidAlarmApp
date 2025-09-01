package com.example.schedulealarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var timePicker: TimePicker
    private lateinit var labelEdit: EditText
    private lateinit var repeatCheck: CheckBox
    private val prefs by lazy { getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE) }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        timePicker = findViewById(R.id.timePicker)
        labelEdit = findViewById(R.id.labelEdit)
        repeatCheck = findViewById(R.id.repeatCheck)

        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this))

        findViewById<Button>(R.id.setButton).setOnClickListener {
            scheduleAlarm()
        }
        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            cancelAlarm()
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val savedHour = prefs.getInt("hour", 8)
        val savedMinute = prefs.getInt("minute", 0)
        val savedLabel = prefs.getString("label", "") ?: ""
        val savedRepeat = prefs.getBoolean("repeat", false)
        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.hour = savedHour
            timePicker.minute = savedMinute
        } else {
            timePicker.currentHour = savedHour
            timePicker.currentMinute = savedMinute
        }
        labelEdit.setText(savedLabel)
        repeatCheck.isChecked = savedRepeat
    }

    private fun scheduleAlarm() {
        val hour: Int
        val minute: Int
        if (Build.VERSION.SDK_INT >= 23) {
            hour = timePicker.hour
            minute = timePicker.minute
        } else {
            hour = timePicker.currentHour
            minute = timePicker.currentMinute
        }
        val label = labelEdit.text?.toString()?.trim() ?: ""
        val repeat = repeatCheck.isChecked

        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("label", label)
        }

        val pending = PendingIntent.getBroadcast(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val showIntent = Intent(this, MainActivity::class.java)
        val showPending = PendingIntent.getActivity(
            this, 2001, showIntent, PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val info = AlarmManager.AlarmClockInfo(cal.timeInMillis, showPending)
        alarmManager.setAlarmClock(info, pending)

        prefs.edit()
            .putInt("hour", hour)
            .putInt("minute", minute)
            .putString("label", label)
            .putBoolean("repeat", repeat)
            .putLong("triggerAt", cal.timeInMillis)
            .apply()

        Toast.makeText(this, "Alarm set for %02d:%02d".format(hour, minute), Toast.LENGTH_LONG).show()
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            this,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pending)
        prefs.edit().remove("triggerAt").apply()
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder("alarm_channel", NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName(getString(R.string.notification_channel_name))
            .setDescription(getString(R.string.notification_channel_desc))
            .setVibrationEnabled(true)
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }
}