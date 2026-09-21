package ru.plumsoftware.notepad.data.model

import kotlinx.serialization.Serializable

/**
 * Прикреплённый к заметке документ (pdf/word/excel и т.п.).
 * [path] — абсолютный путь к копии файла во внутреннем хранилище,
 * [name] — исходное имя файла для показа, [mime] — тип для открытия.
 */
@Serializable
data class NoteFile(
    val path: String,
    val name: String,
    val mime: String
)
