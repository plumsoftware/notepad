package ru.plumsoftware.notepad.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import androidx.work.ExistingWorkPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.plumsoftware.notepad.data.database.GroupWithCount
import ru.plumsoftware.notepad.data.database.habit.HabitRepository
import ru.plumsoftware.notepad.data.model.habit.Habit
import ru.plumsoftware.notepad.data.model.habit.HabitFrequency
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory
import ru.plumsoftware.notepad.data.worker.HabitAlarmScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModel(application: Application, openAddNote: Boolean) : ViewModel() {

    // --- Dependencies ---
    private val db = NoteDatabase.getDatabase(application)
    private val workManager = WorkManager.getInstance(application)
    private val appContext = application.applicationContext

    // --- States ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Query for Search
    private val _searchQuery = MutableStateFlow("")

    // Selected Group ID ("0" = All, "-1" = Secure)
    private val _selectedGroupId = MutableStateFlow("0")
    val selectedGroupId: StateFlow<String> = _selectedGroupId

    // Security Logic
    private val sharedPrefs = appContext.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    var isSecureFolderUnlocked by mutableStateOf(false)
        private set

    // --- MAIN DATA FLOW (Исправленная логика) ---
    // Объединяем ID группы и текст поиска в один поток.
    // Как только что-то меняется, запрос перезапускается.
    val notes: StateFlow<List<Note>> = combine(_selectedGroupId, _searchQuery) { groupId, query ->
        Pair(groupId, query)
    }.flatMapLatest { (groupId, query) ->
        _isLoading.value = true

        // Выбираем правильный запрос
        val flow: Flow<List<Note>> = if (query.isEmpty()) {
            // Режим просмотра списка
            when (groupId) {
                "0" -> db.noteDao().getNormalNotesWithGroups().map { list -> list.map { it.note } }
                SECURE_FOLDER_ID -> {
                    // Грузим секретные ТОЛЬКО если разблокировано
                    if (isSecureFolderUnlocked) {
                        db.noteDao().getSecureNotes().map { list -> list.map { it.note } }
                    } else {
                        // Если зашли "нелегально" (баг UI), не показываем ничего
                        flowOf(emptyList())
                    }
                }

                else -> db.noteDao().getNotesByGroupId(groupId)
            }
        } else {
            // Режим поиска
            when (groupId) {
                "0" -> db.noteDao().searchNotes(query) // Ищет везде КРОМЕ скрытых
                SECURE_FOLDER_ID -> {
                    if (isSecureFolderUnlocked) {
                        db.noteDao().searchNotesInGroup(query, SECURE_FOLDER_ID)
                    } else {
                        flowOf(emptyList())
                    }
                }

                else -> db.noteDao().searchNotesInGroup(query, groupId)
            }
        }
        flow
    }
        .onEach { _isLoading.value = false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Side Data ---
    // Группы со счетчиками
    val groups = db.groupDao().getGroupsWithCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Общее количество заметок
    val totalNotesCount = db.noteDao().getAllNotesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Количество секретных заметок (для замочка)
    val secretNotesCount = db.noteDao().getSecretNotesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val openAddNoteScreen = MutableStateFlow(openAddNote)
    val needToShowRateDialog = MutableStateFlow(false)

    // --- Rating Logic Vars ---
    private val lastRateDialogShownTime = MutableStateFlow(0L)
    private val hasRatedApp = MutableStateFlow(false)

    // --- Repository for habits ---
    private val habitRepository = HabitRepository(db.habitDao())
    val habits: StateFlow<List<HabitWithHistory>> = habitRepository.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    private val habitScheduler = HabitAlarmScheduler(appContext)

    init {
        loadRatePreferences()
    }

    // --- ACTIONS ---

    // Просто обновляем поисковый запрос (список сам перестроится)
    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun selectGroup(groupId: String) {
        // Если выходим из секретной папки -> блокируем её
        if (_selectedGroupId.value == SECURE_FOLDER_ID && groupId != SECURE_FOLDER_ID) {
            isSecureFolderUnlocked = false
        }
        _selectedGroupId.value = groupId
    }

    // --- CRUD ---

    fun addNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Если мы сейчас в какой-то папке, новая заметка должна попасть в неё
                val currentGroup = _selectedGroupId.value
                val finalNote = if (currentGroup != "0") {
                    note.copy(groupId = currentGroup)
                } else {
                    note
                }

                db.noteDao().insert(finalNote)
                if (finalNote.reminderDate != null) {
                    scheduleReminder(finalNote)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(note: Note, context: Context) {
        viewModelScope.launch {
            // Loader не нужен для мелких обновлений типа чекбокса, чтобы не дергать UI
            // _isLoading.value = true
            try {
                db.noteDao().update(note)
                workManager.cancelUniqueWork("reminder_${note.id}")
                if (note.reminderDate != null) {
                    scheduleReminder(note)
                }
            } finally {
                // _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note, context: Context) {
        viewModelScope.launch {
            try {
                db.noteDao().delete(note)
                workManager.cancelUniqueWork("reminder_${note.id}")
                deleteImagesFromStorage(context, note.photos)
            } finally {
            }
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

    fun addFolder(group: Group) {
        viewModelScope.launch { db.groupDao().insert(group) }
    }

    fun deleteFolder(group: Group?) {
        if (group == null) return
        viewModelScope.launch { db.groupDao().delete(group) }
    }

    // --- SECURE LOGIC ---

    fun isPinSet(): Boolean = sharedPrefs.contains(KEY_PIN)

    fun checkPin(inputPin: String): Boolean {
        val savedPin = sharedPrefs.getString(KEY_PIN, "")
        return inputPin == savedPin
    }

    fun savePin(newPin: String) {
        sharedPrefs.edit().putString(KEY_PIN, newPin).apply()
        // Авторизуем сразу после создания
        isSecureFolderUnlocked = true
        selectGroup(SECURE_FOLDER_ID)
    }

    fun unlockSecureFolder() {
        isSecureFolderUnlocked = true
        selectGroup(SECURE_FOLDER_ID)
    }

    // --- HELPERS & RATING ---

    private fun scheduleReminder(note: Note) {
        note.reminderDate?.let { reminderDate ->
            val delay = reminderDate - System.currentTimeMillis()

            // ЛОГ ДЛЯ ОТЛАДКИ
            Log.d("REMINDER", "Planning for note ${note.title}, delay: $delay ms")

            if (delay > 0) {
                val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        workDataOf(
                            "noteId" to note.id,
                            "noteTitle" to note.title,
                            "noteDescription" to note.description
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    "reminder_${note.id}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

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
                putBoolean("has_rated_app", hasRatedApp.value)
            }
        }
    }

    fun checkShouldShowRateDialog(notesCount: Int) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val shouldShow = notesCount > 1 && !hasRatedApp.value &&
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

    fun loading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun getNoteById(noteId: String): Flow<Note?> {
        // Добавь этот метод в NoteDao: @Query("SELECT * FROM notes WHERE id = :id") fun getNoteById(id: String): Flow<Note?>
        // Но пока можно профильтровать текущий список, но это ненадежно.
        // Давай сделаем правильно через БД.
        return db.noteDao().getNoteById(noteId)
    }

    // Сброс пароля (удаление)
    fun resetPin() {
        sharedPrefs.edit().remove(KEY_PIN).apply()
        // Блокируем доступ после сброса (или наоборот, как решишь, обычно сброс = удаление защиты)
        isSecureFolderUnlocked = true
    }

    // Изменение пароля (просто перезапись)
    fun changePin(oldPin: String, newPin: String): Boolean {
        if (checkPin(oldPin)) {
            savePin(newPin)
            return true
        }
        return false
    }

    fun toggleHabitForDate(habitId: String, date: Date) {
        viewModelScope.launch {
            // Конвертируем Date в timestamp начала дня (00:00:00)
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val targetTime = calendar.timeInMillis

            // Вызываем репозиторий (тебе нужно добавить этот метод в репозиторий тоже)
            habitRepository.toggleHabitCompletionForDate(habitId, targetTime)
        }
    }

    //    region::Habits
    fun createHabit(
        title: String,
        color: Long,
        emoji: String,
        isDaily: Boolean,
        days: Set<Int>, // Твой UI возвращает 1..7
        hasReminder: Boolean,
        hour: Int,
        minute: Int
    ) {
        viewModelScope.launch {
            // Конвертируем дни в Calendar константы, если у тебя свой формат
            // Например: если в UI (Пн=1), а в Calendar (Вс=1, Пн=2).
            // Допустим, ты используешь формат Java Calendar везде:
            val repeatDaysList = if (isDaily) emptyList() else days.toList()

            val habit = Habit(
                title = title,
                color = color,
                emoji = emoji,
                frequency = if (isDaily) HabitFrequency.DAILY else HabitFrequency.SPECIFIC_DAYS,
                repeatDays = repeatDaysList,
                isReminderEnabled = hasReminder,
                reminderHour = if (hasReminder) hour else null,
                reminderMinute = if (hasReminder) minute else null
            )

            habitRepository.createHabit(habit)

            if (hasReminder) {
                scheduleHabitReminder(habit)
            }
        }
    }


    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habit)
            // Отменяем будильник
            habitScheduler.cancelReminder(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.updateHabit(habit)
            // Перезапускаем будильник (старый отменится, новый поставится)
            habitScheduler.cancelReminder(habit)
            if (habit.isReminderEnabled) {
                scheduleHabitReminder(habit)
            }
        }
    }

    private fun scheduleHabitReminder(habit: Habit) {
        // Делегируем логику планировщику
        habitScheduler.scheduleNextReminder(habit)
    }

    // Переключение состояния (Выполнено/Нет)
    fun toggleHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleHabitCompletion(habitId)
            // Здесь можно проиграть звук успеха или вибрацию, если передать context или callback
        }
    }
//    endregion

    companion object {
        const val SECURE_FOLDER_ID = "-1"
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_PIN = "secure_pin"
    }
}