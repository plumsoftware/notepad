package ru.plumsoftware.notepad.data.model.habit

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,

    // Внешний вид
    val color: Long,       // Храним как Long (0xFF...)
    val emoji: String,     // Например "💧"

    // Расписание
    val frequency: HabitFrequency,
    val repeatDays: List<Int> = emptyList(), // Список дней: 2=Пн, 3=Вт (формат java.util.Calendar)

    // Напоминание
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val isReminderEnabled: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)