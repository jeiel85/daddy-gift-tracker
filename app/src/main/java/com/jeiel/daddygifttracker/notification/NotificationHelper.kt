package com.jeiel.daddygifttracker.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jeiel.daddygifttracker.MainActivity
import com.jeiel.daddygifttracker.R
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "gyeongjosa_reminders_channel"
    private const val CHANNEL_NAME = "경조사 및 인맥 알림"
    private const val CHANNEL_DESC = "경조사 일정 및 인맥 챙김 알림 채널입니다."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showNotification(context: Context, id: Int, title: String, message: String) {
        // Create channel first
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Fallback to launcher icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(id, builder.build())
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Failed to display notification", e)
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleReminder(context: Context, reminderId: Int, title: String, message: String, dateString: String) {
        // dateString format is "YYYY-MM-DD"
        val parts = dateString.split("-")
        if (parts.size != 3) {
            Log.e("NotificationHelper", "Invalid date format for reminder: $dateString")
            return
        }

        try {
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1 // Calendar is 0-indexed for month
            val day = parts[2].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 9) // Notify at 9 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // If scheduled time has already passed, don't schedule it
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                Log.d("NotificationHelper", "Reminder date $dateString is in the past. Skip scheduling.")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("id", reminderId)
                putExtra("title", title)
                putExtra("message", message)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setAndAllowWhileIdle for reliable trigger on modern Android
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("NotificationHelper", "Scheduled reminder: '$title' at $dateString")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error scheduling alarm", e)
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("NotificationHelper", "Cancelled reminder with ID: $reminderId")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error canceling alarm", e)
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val title = intent.getStringExtra("title") ?: "알림"
        val message = intent.getStringExtra("message") ?: "일정을 확인할 시간입니다."

        NotificationHelper.showNotification(context, id, title, message)
    }
}

