package ru.plumsoftware.notepad.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.database.habit.HabitRepository
import ru.plumsoftware.notepad.data.worker.HabitAlarmScheduler
import java.util.Calendar

class HabitReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("habitId") ?: return
        val title = intent.getStringExtra("title") ?: "Привычка"

        // 1. Сразу показываем уведомление (это быстро)
        showNotification(context, habitId, title)

        // 2. Говорим системе: "Не убивай меня, мне нужно пару секунд на базу данных"
        val pendingResult = goAsync()

        val db = NoteDatabase.getDatabase(context.applicationContext as android.app.Application)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Достаем привычку, чтобы узнать, когда звонить в следующий раз
                val habit = db.habitDao().getHabitById(habitId)
                habit?.let {
                    // Планируем следующий раз
                    HabitAlarmScheduler(context).scheduleNextReminder(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // 3. Сообщаем системе, что мы закончили
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, habitId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Привычки", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habitId.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Используем строковой ресурс или дефолт
        val contentText = context.getString(R.string.habit_notification_text)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_name) // Убедись, что иконка есть, или R.drawable.ic_stat_name
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }
}