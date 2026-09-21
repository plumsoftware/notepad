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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.plumsoftware.notepad.MainActivity
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.NoteFormatting
import ru.plumsoftware.notepad.data.model.TextSpan

class ReminderWorker(
    val appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val noteId = inputData.getString("noteId") ?: return Result.failure()
        val noteTitle = inputData.getString("noteTitle") ?: return Result.failure()
        val noteDescription = inputData.getString("noteDescription") ?: ""
        val noteSpansJson = inputData.getString("noteSpans")
        val ringtoneUriString = inputData.getString("ringtoneUri")
        val ringtoneUri: android.net.Uri? = ringtoneUriString?.let { android.net.Uri.parse(it) }

        val notificationManager = ContextCompat.getSystemService(
            appContext,
            NotificationManager::class.java
        ) as NotificationManager

        // Канал зависит от выбранного рингтона: звук канала фиксируется при создании,
        // поэтому для разных рингтонов используем разные каналы.
        val channelId = if (ringtoneUri != null) {
            "note_reminder_ringtone_${ringtoneUriString.hashCode()}"
        } else {
            "note_reminder_channel_v2"
        }
        val channelName = "Note Reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for note reminders"
                enableVibration(true)
                if (ringtoneUri != null) {
                    val audioAttributes = android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(ringtoneUri, audioAttributes)
                }
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

        // Учитываем форматирование (жирный/курсив/подчёркнутый/зачёркнутый) в тексте уведомления
        val spans: List<TextSpan> = try {
            if (noteSpansJson.isNullOrBlank()) emptyList()
            else Json.decodeFromString(noteSpansJson)
        } catch (e: Exception) {
            emptyList()
        }
        val contentText: CharSequence = if (noteDescription.isBlank()) {
            "Напоминание"
        } else {
            androidx.core.text.HtmlCompat.fromHtml(
                NoteFormatting.spansToHtml(noteDescription, spans),
                androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        val notificationBuilder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(noteTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Для Android < 8.0
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .apply {
                // На Android < 8 звук берётся из уведомления, а не из канала
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && ringtoneUri != null) {
                    setSound(ringtoneUri)
                }
            }

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