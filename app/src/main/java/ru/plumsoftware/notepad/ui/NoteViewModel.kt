package ru.plumsoftware.notepad.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.filesaver.deleteImagesFromStorage
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class NoteViewModel(application: Application) : ViewModel() {
    private val db = NoteDatabase.getDatabase(application)
    private val workManager = WorkManager.getInstance(application)
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
                if (note.reminderDate != null) {
                    scheduleReminder(note)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(note: Note, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.noteDao().update(note)
                workManager.cancelUniqueWork("reminder_${note.id}")
                if (note.reminderDate != null) {
                    scheduleReminder(note)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.noteDao().delete(note)
                workManager.cancelUniqueWork("reminder_${note.id}")
                deleteImagesFromStorage(context, note.photos)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun scheduleReminder(note: Note) {
        note.reminderDate?.let { reminderDate ->
            val delay = reminderDate - System.currentTimeMillis()
            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "noteId" to note.id,
                            "noteTitle" to note.title
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "reminder_${note.id}",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }
}