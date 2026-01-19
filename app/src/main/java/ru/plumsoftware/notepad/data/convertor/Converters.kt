package ru.plumsoftware.notepad.data.convertor

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.plumsoftware.notepad.data.model.Task
import ru.plumsoftware.notepad.data.model.habit.HabitFrequency

class Converters {
    @TypeConverter
    fun fromTaskList(tasks: List<Task>): String {
        return Json.encodeToString(tasks)
    }

    @TypeConverter
    fun toTaskList(tasksString: String): List<Task> {
        return Json.decodeFromString(tasksString)
    }

    @TypeConverter
    fun fromLong(value: Long?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLong(value: String?): Long? {
        return value?.toLongOrNull()
    }

    @TypeConverter
    fun fromStringList(photos: List<String>): String {
        return Json.encodeToString(photos)
    }

    @TypeConverter
    fun toStringList(photosString: String): List<String> {
        return Json.decodeFromString(photosString)
    }

    // Для списков дней (Например: "1,2,3" -> List<Int>)
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").map { it.toInt() }
    }

    // Для Enum (Ежедневно/По дням)
    @TypeConverter
    fun fromFrequency(frequency: HabitFrequency): String = frequency.name

    @TypeConverter
    fun toFrequency(value: String): HabitFrequency = try {
        HabitFrequency.valueOf(value)
    } catch (e: Exception) {
        HabitFrequency.DAILY // Fallback
    }
}