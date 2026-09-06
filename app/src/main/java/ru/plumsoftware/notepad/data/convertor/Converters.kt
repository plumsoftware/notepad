package ru.plumsoftware.notepad.data.convertor

import android.util.Log
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
        return try {
            Json.decodeFromString(tasksString)
        } catch (e: Exception) {
            Log.e("Converters", "Corrupted tasks JSON, falling back to empty list: $tasksString", e)
            emptyList()
        }
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

    // Та же защита, что и для toTaskList — одна повреждённая запись
    // не должна блокировать открытие всего приложения навсегда.
    @TypeConverter
    fun toStringList(photosString: String): List<String> {
        return try {
            Json.decodeFromString(photosString)
        } catch (e: Exception) {
            Log.e("Converters", "Corrupted photos JSON, falling back to empty list: $photosString", e)
            emptyList()
        }
    }

    // Для списков дней (Например: "1,2,3" -> List<Int>)
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.trim().toIntOrNull() }
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