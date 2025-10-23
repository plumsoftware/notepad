package ru.plumsoftware.notepad.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "groups")
class Group(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val color: Long,
    val createdAt: Long = System.currentTimeMillis(),
)