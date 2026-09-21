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
    val photos: List<String> = emptyList(),
    val groupId: String = "0",
    val isPinned: Boolean = false,
    // Форматирование тела заметки (жирный/курсив/подчёркнутый/зачёркнутый)
    val descriptionSpans: List<TextSpan> = emptyList(),
    // Прикреплённые теги
    val tagIds: List<String> = emptyList(),
    // Прикреплённые документы (pdf/word/excel)
    val files: List<NoteFile> = emptyList(),
    // Голосовая заметка
    val voicePath: String? = null,
    val voiceTranscription: String? = null,
    // Рингтон для уведомления-напоминания
    val ringtoneUri: String? = null,
    val ringtoneTitle: String? = null,
    // Корзина
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
) {
    override fun toString(): String {
        return """
            title: $title;\n
            description: $description\n
            color: $color\n
            createdAt: $createdAt\n
        """.trimIndent()
    }
}
