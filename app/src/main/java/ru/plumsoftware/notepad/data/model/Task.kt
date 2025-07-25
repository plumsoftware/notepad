package ru.plumsoftware.notepad.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isChecked: Boolean = false
)