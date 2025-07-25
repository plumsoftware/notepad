package ru.plumsoftware.notepad.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.model.Note

class NoteViewModel(application: Application) : ViewModel() {
    private val db = NoteDatabase.getDatabase(application)
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        viewModelScope.launch {
            db.noteDao().getAllNotes().collectLatest { notes ->
                _notes.value = notes
            }
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            db.noteDao().searchNotes(query).collectLatest { notes ->
                _notes.value = notes
            }
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            db.noteDao().insert(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            db.noteDao().update(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            db.noteDao().delete(note)
        }
    }
}