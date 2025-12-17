package ru.plumsoftware.notepad.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.R

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val noteId = inputData.getString("noteId") ?: return Result.failure()
        val noteTitle = inputData.getString("noteTitle") ?: return Result.failure()
        // 1. Получаем описание (если null, то пустая строка)
        val noteDescription = inputData.getString("noteDescription") ?: ""

        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        // ... (Код создания канала channelId оставляем как был в прошлом ответе) ...
        // Если нужно, я могу продублировать код канала здесь
        val channelId = "note_reminder_channel_high"

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("scrollToNote", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Определяем текст для показа
        // Если описание пустое, пишем стандартный текст, иначе - само описание
        val contentText = if (noteDescription.isNotBlank()) noteDescription else "Напоминание о заметке"

        val notificationBuilder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(noteTitle)
            .setContentText(contentText) // Ставим текст сюда
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // 3. ВАЖНО: Добавляем BigTextStyle
        // Если описание длинное, оно развернется полностью
        if (noteDescription.isNotBlank()) {
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(noteDescription)
            )
        }

        val notification = notificationBuilder.build()

        // ... (Код проверки разрешений и notify оставляем как был) ...
        notificationManager.notify(noteId.hashCode(), notification)

        return Result.success()
    }
}