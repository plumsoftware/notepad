package ru.plumsoftware.notepad.widget

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.first
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory
import java.util.Calendar

data class HabitWidgetItem(
    val id: String,
    val title: String,
    val emoji: String,
    val doneToday: Boolean
)

object WidgetDataProvider {
    suspend fun getRecentNotes(context: Context, limit: Int = 3): List<Note> {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        return db.noteDao().getAllNotes().first()
            .filter { it.groupId != "-1" }
            .take(limit)
    }

    suspend fun getHabitsForToday(context: Context): List<HabitWidgetItem> {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        val habits = db.habitDao().getAllHabitsWithHistory().first()
        val todayStart = startOfToday()
        return habits.map { item ->
            val done = item.history.any { it.date == todayStart }
            HabitWidgetItem(
                id = item.habit.id,
                title = item.habit.title,
                emoji = item.habit.emoji,
                doneToday = done
            )
        }
    }

    private fun startOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
