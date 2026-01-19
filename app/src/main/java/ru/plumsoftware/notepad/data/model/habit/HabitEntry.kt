package ru.plumsoftware.notepad.data.model.habit

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

// Таблица истории выполнения
// Хранит факт того, что привычка X была выполнена в день Y
@Entity(
    tableName = "habit_history",
    primaryKeys = ["habitId", "date"], // Составной ключ: одну привычку нельзя выполнить дважды за один "логический" день
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE // Если удалить привычку, история удалится сама
        )
    ],
    indices = [Index("habitId")]
)
data class HabitEntry(
    val habitId: String,
    val date: Long, // ВАЖНО: Сюда пишем время начала дня (00:00:00), чтобы группировать по дням
    val completedAt: Long = System.currentTimeMillis() // Реальное время нажатия (для статистики)
)