package ru.plumsoftware.notepad.data.worker

import android.annotation.SuppressLint
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
        // Если напоминание выключено или время не задано - выходим
        if (!habit.isReminderEnabled || habit.reminderHour == null || habit.reminderMinute == null) return

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        // Мы будем искать подходящее время в ближайшие 8 дней
        // (8 дней - чтобы наверняка захватить следующую неделю, если сегодня тот же день, но время прошло)
        for (i in 0..8) {
            calendar.timeInMillis = now // Сброс на "сейчас"
            calendar.add(Calendar.DAY_OF_YEAR, i) // Прибавляем i дней

            // Устанавливаем время из привычки
            calendar.set(Calendar.HOUR_OF_DAY, habit.reminderHour)
            calendar.set(Calendar.MINUTE, habit.reminderMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Важнейшая проверка:
            // Если вычисленное время (сегодня 9:00) уже прошло (сейчас 9:01),
            // то calendar.timeInMillis < now. Мы это время пропускаем.
            // Добавляем +1 минуту запаса на всякий случай.
            if (calendar.timeInMillis <= (now + 60_000)) {
                continue
            }

            // Теперь проверяем, подходит ли этот день недели
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Вс, 2=Пн ...

            val isDayMatching = when (habit.frequency) {
                HabitFrequency.DAILY -> true // Каждый день подходит
                HabitFrequency.SPECIFIC_DAYS -> habit.repeatDays.contains(dayOfWeek) // Только выбранные
            }

            if (isDayMatching) {
                // Нашли! Ставим таймер и выходим из цикла
                setAlarm(habit, calendar.timeInMillis)
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

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(habit: Habit, triggerAtMillis: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habitId", habit.id)
            putExtra("title", habit.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Используем setAndAllowWhileIdle - это позволяет сработать даже в спящем режиме,
        // но может быть небольшая задержка (что тебе ок).
        // Это самый надежный способ без жестких прав exact alarm.
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}