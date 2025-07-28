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
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _isLoading.value = true
            db.noteDao().getAllNotes().collectLatest { notes ->
                _notes.value = notes
                _isLoading.value = false
            }
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            db.noteDao().searchNotes(query).collectLatest { notes ->
                _notes.value = notes
                _isLoading.value = false
            }
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.noteDao().insert(note)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.noteDao().update(note)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.noteDao().delete(note)
            } finally {
                _isLoading.value = false
            }
        }
    }
}