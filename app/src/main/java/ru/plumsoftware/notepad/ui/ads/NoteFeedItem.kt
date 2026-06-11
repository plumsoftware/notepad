package ru.plumsoftware.notepad.ui.ads

import ru.plumsoftware.notepad.data.model.Note

sealed class NoteFeedItem(val key: String) {
    data class NoteEntry(val note: Note) : NoteFeedItem("note_${note.id}")
    data class NativeAdSlot(val index: Int) : NoteFeedItem("native_ad_$index")
}

fun buildNoteFeedWithAds(notes: List<Note>): List<NoteFeedItem> {
    if (notes.size < 3) {
        return notes.map { NoteFeedItem.NoteEntry(it) }
    }

    val result = ArrayList<NoteFeedItem>(notes.size + notes.size / 3)
    notes.forEachIndexed { index, note ->
        result.add(NoteFeedItem.NoteEntry(note))
        // Реклама после каждых 3 заметок: 3, 6, 9, …
        if ((index + 1) % 3 == 0) {
            result.add(NoteFeedItem.NativeAdSlot((index + 1) / 3 - 1))
        }
    }
    return result
}
