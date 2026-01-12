package ru.plumsoftware.notepad.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.R

class ReminderWorker(
    val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val noteId = inputData.getString("noteId") ?: return Result.failure()
        val noteTitle = inputData.getString("noteTitle") ?: return Result.failure()
        val noteDescription = inputData.getString("noteDescription") ?: ""

        // ИСПРАВЛЕНИЕ 1: Единый ID канала (тот же, что в MainActivity, или создадим заново)
        val channelId = "note_reminder_channel_v2"
        val channelName = "Note Reminders"

        // ИСПРАВЛЕНИЕ 2: Создаем канал прямо тут, чтобы гарантировать его существование
        val notificationManager = ContextCompat.getSystemService(
            appContext,
            NotificationManager::class.java
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for note reminders"
                enableVibration(true) // Включаем вибрацию
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Подготовка интента для открытия приложения по клику
        val intent = Intent(appContext, MainActivity::class.java).apply {
            putExtra("noteId", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            noteId.hashCode(), // Уникальный ID для интента
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Иконка (Убедись, что этот ресурс существует, иначе используй R.drawable.ic_launcher_foreground)
        // Если R.drawable.ic_stat_name нет, замени на стандартную
        val smallIcon = try {
            R.drawable.ic_stat_name
        } catch (e: Exception) {
            R.drawable.full_icon // Или любая существующая иконка
        }

        val contentText = noteDescription.ifBlank { "Напоминание" }

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(noteTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Для Android < 8.0
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))

        // ИСПРАВЛЕНИЕ 3: Проверка разрешений для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Если разрешения нет, мы не можем отправить уведомление.
                // Можно попробовать выкинуть Failure или Success, но уведомления не будет.
                return Result.failure()
            }
        }

        notificationManager.notify(noteId.hashCode(), notificationBuilder.build())

        return Result.success()
    }
}