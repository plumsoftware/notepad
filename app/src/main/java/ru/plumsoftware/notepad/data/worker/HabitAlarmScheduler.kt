package ru.plumsoftware.notepad.data.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import ru.plumsoftware.notepad.data.model.habit.Habit
import ru.plumsoftware.notepad.data.model.habit.HabitFrequency
import ru.plumsoftware.notepad.data.receiver.HabitReminderReceiver
import java.util.Calendar

class HabitAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNextReminder(habit: Habit) {
        if (!habit.isReminderEnabled || habit.reminderHour == null || habit.reminderMinute == null) return

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        // Находим БЛИЖАЙШЕЕ время выполнения
        // Проверяем 7 дней вперед
        for (i in 0..7) {
            val candidate = Calendar.getInstance()
            candidate.add(Calendar.DAY_OF_YEAR, i)
            candidate.set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            candidate.set(Calendar.MINUTE, habit.reminderMinute)
            candidate.set(Calendar.SECOND, 0)
            candidate.set(Calendar.MILLISECOND, 0)

            // Если это время уже прошло СЕГОДНЯ, пропускаем
            if (candidate.timeInMillis <= now) continue

            // Проверяем, подходит ли день недели
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

            // В Java Calendar воскресенье = 1. Твой UI может использовать другую логику.
            // HabitFrequency.DAILY всегда подходит.
            // HabitFrequency.SPECIFIC_DAYS - проверяем в списке.

            // Здесь предположим, что repeatDays хранит [Calendar.MONDAY, Calendar.FRIDAY]
            val isDayMatching = when (habit.frequency) {
                HabitFrequency.DAILY -> true
                HabitFrequency.SPECIFIC_DAYS -> habit.repeatDays.contains(dayOfWeek)
            }

            if (isDayMatching) {
                // Нашли! Ставим будильник на это время
                setAlarm(habit, candidate.timeInMillis)
                return
            }
        }
    }

    fun cancelReminder(habit: Habit) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun setAlarm(habit: Habit, triggerAtMillis: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habitId", habit.id)
            putExtra("title", habit.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(), // Уникальный ID для каждой привычки
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Используем точный будильник для надежности
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                // Если нет прав на точные, ставим обычный (юзеру стоит показать настройки, но для MVP так сойдет)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }

        Log.d("HABIT_SCHEDULER", "Scheduled ${habit.title} at $triggerAtMillis")
    }
}