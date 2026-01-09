package ru.plumsoftware.notepad.data.database

import androidx.room.Embedded
import androidx.room.ColumnInfo
import ru.plumsoftware.notepad.data.model.Note

data class NoteWithGroupInfo(
    @Embedded val note: Note, // Встраиваем всю заметку целиком

    @ColumnInfo(name = "group_title")
    val groupTitle: String?, // Название группы (может быть null, если группа удалена)

    @ColumnInfo(name = "group_color")
    val groupColor: Long?    // Цвет группы
)