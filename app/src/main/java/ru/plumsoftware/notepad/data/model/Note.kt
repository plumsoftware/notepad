package ru.plumsoftware.notepad.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val tasks: List<Task> = emptyList(),
    val reminderDate: Long? = null,
    val photos: List<String> = emptyList()
)