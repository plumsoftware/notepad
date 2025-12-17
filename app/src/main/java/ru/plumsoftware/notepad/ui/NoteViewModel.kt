package ru.plumsoftware.notepad.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.filesaver.deleteImagesFromStorage
import ru.plumsoftware.notepad.data.model.Group
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(application: Application, openAddNote: Boolean) : ViewModel() {
    private val db = NoteDatabase.getDatabase(application)
    private val workManager = WorkManager.getInstance(application)
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    private val _groups = MutableStateFlow<List<Group>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes
    val groups: StateFlow<List<Group>> = _groups
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val appContext = application.applicationContext

    private val _openAddNoteScreen = MutableStateFlow(openAddNote)
    val openAddNoteScreen: StateFlow<Boolean> = _openAddNoteScreen

    // --- РЕЙТИНГ ---
    val needToShowRateDialog = MutableStateFlow(false)

    // Новые состояния для отслеживания времени
    private val lastRateDialogShownTime = MutableStateFlow(0L)
    private val hasRatedApp = MutableStateFlow(false)

    private fun loadRatePreferences() {
        viewModelScope.launch {
            val prefs = appContext.getSharedPreferences("app_rating", Context.MODE_PRIVATE)
            lastRateDialogShownTime.value = prefs.getLong("last_rate_dialog_time", 0L)
            hasRatedApp.value = prefs.getBoolean("has_rated_app", false)
        }
    }

    @SuppressLint("UseKtx")
    private suspend fun saveRatePreferences() {
        withContext(Dispatchers.IO) {
            val prefs = appContext.getSharedPreferences("app_rating", Context.MODE_PRIVATE)
            prefs.edit {
                putLong("last_rate_dialog_time", lastRateDialogShownTime.value)
                    .putBoolean("has_rated_app", hasRatedApp.value)
                }
        }
    }

    fun checkShouldShowRateDialog(notesCount: Int) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L

            val shouldShow = notesCount > 1 &&
                    !hasRatedApp.value &&
                    (currentTime - lastRateDialogShownTime.value) > oneDayInMillis

            needToShowRateDialog.value = shouldShow

            if (shouldShow) {
                lastRateDialogShownTime.value = currentTime
                saveRatePreferences()
            }
        }
    }

    fun setAppRated() {
        viewModelScope.launch {
            hasRatedApp.value = true
            needToShowRateDialog.value = false
            saveRatePreferences()
        }
    }

    fun setNeedToShowRateDialog(show: Boolean) {
        needToShowRateDialog.value = show
    }

    private val _selectedGroupId = MutableStateFlow<String>("0")
    val selectedGroupId: StateFlow<String> = _selectedGroupId

    init {
        viewModelScope.launch {
            db.groupDao().getAllGroups().collectLatest { groups ->
                _groups.value = groups
            }
        }

        viewModelScope.launch {
            _selectedGroupId
                .flatMapLatest { groupId ->
                    if (groupId == "0") {
                        db.noteDao().getAllNotes()
                    } else {
                        db.noteDao().getNotesByGroupId(groupId)
                    }
                }
                .collect { notes ->
                    _notes.value = notes
                    _isLoading.value = false
                }
        }
        loadRatePreferences()
    }

    fun selectGroup(groupId: String) {
        if (_selectedGroupId.value != groupId) {
            _selectedGroupId.value = groupId
            _isLoading.value = true
        }
    }

    fun moveNoteToGroup(note: Note, targetGroupId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedNote = note.copy(groupId = targetGroupId)
                db.noteDao().update(updatedNote)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentGroupId = _selectedGroupId.value
            val flow = if (query.isEmpty()) {
                if (currentGroupId == "0") {
                    db.noteDao().getAllNotes()
                } else {
                    db.noteDao().getNotesByGroupId(currentGroupId)
                }
            } else {
                if (currentGroupId == "0") {
                    db.noteDao().searchNotes(query)
                } else {
                    db.noteDao().searchNotesInGroup(query, currentGroupId)
                }
            }
            flow.collect { notes ->
                _notes.value = notes
                _isLoading.value = false
            }
        }
    }

    fun addFolder(group: Group) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.groupDao().insert(group)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFolder(group: Group?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (group != null)
                    db.groupDao().delete(group)
            } finally {
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

            // Логи (оставляем, раз они нужны)
            Log.d("REMINDER_DEBUG", "Plan reminder for: ${note.title}, Delay: $delay")

            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "noteId" to note.id,
                            "noteTitle" to note.title,
                            "noteDescription" to note.description // <--- ДОБАВИЛИ ЭТУ СТРОКУ
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

    // Вспомогательная функция для логов, если её нет в классе
    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    fun loading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }
}