package ru.plumsoftware.notepad.data.database

import androidx.room.Embedded
import ru.plumsoftware.notepad.data.model.Group

data class GroupWithCount(
    @Embedded val group: Group,
    val noteCount: Int
)