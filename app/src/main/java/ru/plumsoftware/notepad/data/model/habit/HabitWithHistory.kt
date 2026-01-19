package ru.plumsoftware.notepad.data.model.habit

import androidx.room.Embedded
import androidx.room.Relation

data class HabitWithHistory(
    @Embedded val habit: Habit,

    @Relation(
        parentColumn = "id",
        entityColumn = "habitId"
    )
    val history: List<HabitEntry>
)