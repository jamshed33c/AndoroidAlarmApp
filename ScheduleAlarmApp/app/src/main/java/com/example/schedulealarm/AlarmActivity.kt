package com.example.schedulealarm

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmActivity : AppCompatActivity() {
    private var ringtone: Ringtone? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val label = intent.getStringExtra("label") ?: ""
        findViewById<TextView>(R.id.alarmLabel).text = label

        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopAlarm()
            finish()
        }

        startAlarm()
    }

    private fun startAlarm() {
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, uri).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            play()
        }

        val vib = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(500)
        acquireWakelock()
    }

    private fun acquireWakelock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "ScheduleAlarm:AlarmWakeLock"
        )
        wakeLock?.setReferenceCounted(false)
        wakeLock?.acquire(60_000)
    }

    private fun stopAlarm() {
        try { ringtone?.stop() } catch (_: Exception) { }
        wakeLock?.let { if (it.isHeld) it.release() }
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}