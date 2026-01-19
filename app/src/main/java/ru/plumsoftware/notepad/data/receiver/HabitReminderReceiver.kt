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

        // 1. Показываем уведомление
        showNotification(context, habitId, title)

        // 2. Планируем СЛЕДУЮЩЕЕ напоминание (Рекурсия)
        // Нам нужно достать привычку из базы, чтобы узнать расписание
        val db = NoteDatabase.getDatabase(context.applicationContext as android.app.Application)
        val repo = HabitRepository(db.habitDao())

        CoroutineScope(Dispatchers.IO).launch {
            // Внимание: Здесь нам нужен метод в DAO getHabitById(id)
            // Добавь в HabitDao: @Query("SELECT * FROM habits WHERE id = :id") suspend fun getHabitById(id: String): Habit?
            val habit = db.habitDao().getHabitById(habitId)
            habit?.let {
                HabitAlarmScheduler(context).scheduleNextReminder(it)
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.full_icon) // Твоя иконка
            .setContentTitle(title)
            .setContentText("Время выполнить привычку!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }
}