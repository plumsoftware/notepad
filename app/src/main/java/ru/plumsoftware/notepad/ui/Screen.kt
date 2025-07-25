package ru.plumsoftware.notepad.ui

sealed class Screen(val route: String) {
    data object NoteList : Screen("note_list")
    data object AddNote : Screen("add_note")
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/$noteId"
    }
}