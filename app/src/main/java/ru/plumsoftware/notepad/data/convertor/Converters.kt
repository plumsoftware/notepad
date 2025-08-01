package ru.plumsoftware.notepad.data.convertor

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.plumsoftware.notepad.data.model.Task

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
}