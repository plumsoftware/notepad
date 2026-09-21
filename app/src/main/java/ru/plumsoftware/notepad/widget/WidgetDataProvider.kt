package ru.plumsoftware.notepad.widget

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.flow.first
import ru.plumsoftware.notepad.data.database.NoteDatabase
import ru.plumsoftware.notepad.data.model.Note
import java.util.Calendar

/** Заметка за сегодня с готовой строкой-мета для виджета «Сегодня». */
data class TodayNoteItem(
    val id: String,
    val title: String,
    val colorLong: Long,
    val meta: String
)

/** Ближайшее напоминание для виджета «Ближайшее». */
data class ReminderItem(
    val id: String,
    val title: String,
    val colorLong: Long,
    val subtitle: String,
    val emoji: String
)

/** Задача внутри закреплённой заметки. */
data class PinnedTask(val text: String, val checked: Boolean)

/** Данные закреплённой заметки для виджета «Закреплённое». */
data class PinnedNoteItem(
    val id: String,
    val title: String,
    val description: String,
    val colorLong: Long,
    val dateText: String,
    val tasks: List<PinnedTask>,
    val hasVoice: Boolean,
    val voiceDurationText: String,
    val voiceTranscription: String
)

object WidgetDataProvider {
    private val urlRegex = Regex(
        "(https?://[^\\s]+|www\\.[^\\s]+|[a-zA-Z0-9][a-zA-Z0-9\\-]*(?:\\.[a-zA-Z0-9\\-]+)+(?:/[^\\s]*)?)"
    )

    /** Заметки, созданные сегодня (для виджета «Сегодня»). */
    suspend fun getTodayNotes(context: Context, limit: Int = 3): List<TodayNoteItem> {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        val start = startOfToday()
        val end = start + 24L * 60 * 60 * 1000
        return db.noteDao().getAllNotes().first()
            .filter { it.groupId != "-1" && it.createdAt in start until end }
            .take(limit)
            .map { note ->
                TodayNoteItem(
                    id = note.id,
                    title = note.title.ifBlank { "Без названия" },
                    colorLong = note.color,
                    meta = buildMeta(note)
                )
            }
    }

    suspend fun getTodayCount(context: Context): Int {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        val start = startOfToday()
        val end = start + 24L * 60 * 60 * 1000
        return db.noteDao().getAllNotes().first()
            .count { it.groupId != "-1" && it.createdAt in start until end }
    }

    /** Ближайшие напоминания (для виджета «Ближайшее»). */
    suspend fun getUpcomingReminders(context: Context, limit: Int = 3): List<ReminderItem> {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        val now = System.currentTimeMillis()
        return db.noteDao().getAllNotes().first()
            .filter { it.groupId != "-1" && it.reminderDate != null && it.reminderDate >= now }
            .sortedBy { it.reminderDate }
            .take(limit)
            .map { note ->
                val time = formatTime(note.reminderDate!!)
                val sub = buildString {
                    append(time)
                    if (!note.ringtoneTitle.isNullOrBlank()) append(" · рингтон «${note.ringtoneTitle}»")
                    else if (note.tasks.isNotEmpty()) append(" · ${plural(note.tasks.size, "пункт", "пункта", "пунктов")}")
                }
                ReminderItem(
                    id = note.id,
                    title = note.title.ifBlank { "Напоминание" },
                    colorLong = note.color,
                    subtitle = sub,
                    emoji = when {
                        note.tasks.isNotEmpty() -> "🛒"
                        else -> "🔔"
                    }
                )
            }
    }

    /** Самая свежая закреплённая заметка (для виджета «Закреплённое»). */
    suspend fun getPinnedNote(context: Context): PinnedNoteItem? {
        val db = NoteDatabase.getDatabase(context.applicationContext as Application)
        val note = db.noteDao().getAllNotes().first()
            .firstOrNull { it.isPinned && it.groupId != "-1" } ?: return null
        return PinnedNoteItem(
            id = note.id,
            title = note.title.ifBlank { "Без названия" },
            description = note.description.substringBefore('\n'),
            colorLong = note.color,
            dateText = formatShortDate(note.createdAt),
            tasks = note.tasks.take(2).map { PinnedTask(it.text, it.isChecked) },
            hasVoice = !note.voicePath.isNullOrBlank(),
            voiceDurationText = note.voicePath?.let { voiceDurationText(it) } ?: "",
            voiceTranscription = note.voiceTranscription?.trim() ?: ""
        )
    }

    private fun buildMeta(note: Note): String = when {
        !note.voicePath.isNullOrBlank() -> "🎙 " + voiceDurationText(note.voicePath)
        note.files.isNotEmpty() -> plural(note.files.size, "файл", "файла", "файлов")
        countLinks(note.description) > 0 -> {
            val n = countLinks(note.description)
            plural(n, "ссылка", "ссылки", "ссылок")
        }
        note.tasks.isNotEmpty() -> plural(note.tasks.size, "пункт", "пункта", "пунктов")
        note.photos.isNotEmpty() -> plural(note.photos.size, "фото", "фото", "фото")
        else -> note.description.substringBefore('\n').take(40)
    }

    private fun countLinks(text: String): Int =
        urlRegex.findAll(text).count { m -> m.value.any { it.isLetter() } }

    private fun voiceDurationText(path: String): String {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val ms = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            formatMillis(ms)
        } catch (e: Exception) {
            ""
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun formatMillis(ms: Long): String {
        val totalSec = (ms / 1000).toInt()
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }

    private fun formatTime(time: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = time }
        return "%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private val monthsShort = listOf(
        "янв", "фев", "мар", "апр", "мая", "июн",
        "июл", "авг", "сен", "окт", "ноя", "дек"
    )

    private fun formatShortDate(time: Long): String {
        val c = Calendar.getInstance().apply { timeInMillis = time }
        return "${c.get(Calendar.DAY_OF_MONTH)} ${monthsShort[c.get(Calendar.MONTH)]}"
    }

    private fun plural(n: Int, one: String, few: String, many: String): String {
        val mod10 = n % 10
        val mod100 = n % 100
        val word = when {
            mod10 == 1 && mod100 != 11 -> one
            mod10 in 2..4 && mod100 !in 12..14 -> few
            else -> many
        }
        return "$n $word"
    }

    private fun startOfToday(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    /** Дни недели вокруг сегодня для верхней полоски виджета «Ближайшее». */
    fun weekStrip(): List<WeekDay> {
        val today = Calendar.getInstance()
        val todayDom = today.get(Calendar.DAY_OF_MONTH)
        val result = mutableListOf<WeekDay>()
        for (offset in -2..4) {
            val c = today.clone() as Calendar
            c.add(Calendar.DAY_OF_MONTH, offset)
            val dow = c.get(Calendar.DAY_OF_WEEK)
            result.add(
                WeekDay(
                    day = c.get(Calendar.DAY_OF_MONTH),
                    isToday = c.get(Calendar.DAY_OF_MONTH) == todayDom && offset == 0,
                    isWeekend = dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
                )
            )
        }
        return result
    }

    fun currentMonthName(): String {
        val months = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        return months[Calendar.getInstance().get(Calendar.MONTH)]
    }
}

data class WeekDay(val day: Int, val isToday: Boolean, val isWeekend: Boolean)
