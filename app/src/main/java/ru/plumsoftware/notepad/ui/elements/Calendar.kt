package ru.plumsoftware.notepad.ui.elements

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Note
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Обновляем data class для хранения информации о неделе
data class CalendarDay(
    val date: Date,
    val isCurrentMonth: Boolean,
    val week: Int, // Добавляем номер недели
    val notes: List<Note> = emptyList()
)

@Composable
fun CalendarView(
    notes: List<Note> = emptyList(),
    selectedDate: Date? = null,
    selectedWeek: Int? = null,
    isScrolled: Boolean = false,
    onDayClick: (Date, List<Note>, Int) -> Unit = { _, _, _ -> },
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    var currentDate by remember { mutableStateOf(Calendar.getInstance()) }

    val calendarDays = remember(currentDate, notes) {
        generateCalendarDays(currentDate, notes)
    }

    Column(modifier = modifier) {
        // Header with navigation
        CalendarHeader(
            currentDate = currentDate,
            onPreviousClick = {
                currentDate = Calendar.getInstance().apply {
                    time = currentDate.time
                    add(Calendar.MONTH, -1)
                }
            },
            onNextClick = {
                currentDate = Calendar.getInstance().apply {
                    time = currentDate.time
                    add(Calendar.MONTH, 1)
                }
            }
        )

        // Days of week
        DaysOfWeekHeader()

        // Calendar grid с группировкой по неделям
        CalendarGrid(
            days = calendarDays,
            selectedDate = selectedDate,
            selectedWeek = selectedWeek,
            onDayClick = onDayClick
        )
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val daysOfWeek = listOf(
        stringResource(R.string.monday),
        stringResource(R.string.tuesday),
        stringResource(R.string.wednesday),
        stringResource(R.string.thursday),
        stringResource(R.string.friday),
        stringResource(R.string.saturday),
        stringResource(R.string.sunday),
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp)
    ) {
        daysOfWeek.forEach { day ->
            Text(
                text = day,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentDate: Calendar,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally
        )
    ) {
        // Previous month button
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }

        // Month and year
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(120.dp)
        ) {
            Text(
                text = stringResource(getMonthName(currentDate)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = getYear(currentDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Next month button
        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    selectedDate: Date? = null,
    selectedWeek: Int? = null,
    onDayClick: (Date, List<Note>, Int) -> Unit
) {
    // Группируем дни по неделям
    val weeks = remember(days) {
        days.chunked(7)
    }

    LazyColumn(
        modifier = Modifier.wrapContentHeight()
    ) {
        itemsIndexed(weeks) { weekIndex, weekDays ->
            // ЛОГИКА ИЗМЕНЕНА:
            // Показываем неделю, если:
            // 1. Никакая дата не выбрана (selectedWeek == null)
            // 2. ИЛИ это именно выбранная неделя
            val shouldShowWeek = selectedWeek == null || weekIndex == selectedWeek

            AnimatedVisibility(
                visible = shouldShowWeek,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                WeekRow(
                    weekDays = weekDays,
                    selectedDate = selectedDate,
                    onDayClick = { date, notes ->
                        onDayClick(date, notes, weekIndex)
                    }
                )
            }
        }
    }
}

@Composable
private fun WeekRow(
    weekDays: List<CalendarDay>,
    selectedDate: Date? = null,
    onDayClick: (Date, List<Note>) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        weekDays.forEach { day ->
            CalendarDayItem(
                day = day,
                selectedDate = selectedDate,
                onDayClick = onDayClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarDayItem(
    day: CalendarDay,
    selectedDate: Date? = null,
    onDayClick: (Date, List<Note>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNumber = getDayNumber(day.date)

    // Проверяем, выбран ли этот день
    val isSelected = selectedDate?.let {
        isSameDay(it, day.date)
    } ?: false

    // Цвета для состояния (текущий месяц или нет)
    val textColor = if (day.isCurrentMonth) {
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }

    // Фон выбранного элемента (круглый или squircle)
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.medium) // Medium (около 12-16dp) выглядит лучше для календаря
            .background(backgroundColor)
            .clickable(
                enabled = true,
                onClick = { onDayClick(day.date, day.notes) },
                role = Role.Button
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // --- ЛОГИКА ТОЧЕК (DOTS) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(6.dp) // Фиксированная высота, чтобы ячейки не скакали
        ) {
            if (day.notes.isNotEmpty()) {
                // Берем максимум 3 заметки
                val notesToShow = day.notes.take(3)

                notesToShow.forEach { note ->
                    // Рисуем точку цветом заметки
                    Box(
                        modifier = Modifier
                            .size(6.dp) // Размер точки
                            .clip(CircleShape)
                            .background(Color(note.color.toULong()))
                    )
                }

                // Если заметок больше 3, можно показать маленькую серую точку или просто оставить 3
                // Вариант: если > 3, третья точка чуть меньше или другого цвета?
                // Но обычно 3 точки достаточно, чтобы понять "много дел"
            }
        }
    }
}

// Обновляем функцию генерации дней для включения номера недели
private fun generateCalendarDays(currentDate: Calendar, notes: List<Note>): List<CalendarDay> {
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    val days = mutableListOf<CalendarDay>()
    var currentWeek = 0

    // Get first day of month and adjust to Monday
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
    val daysFromPreviousMonth = when (firstDayOfMonth) {
        Calendar.MONDAY -> 0
        Calendar.SUNDAY -> 6
        else -> firstDayOfMonth - Calendar.MONDAY
    }

    // Add days from previous month
    calendar.add(Calendar.DAY_OF_MONTH, -daysFromPreviousMonth)
    for (i in 0 until daysFromPreviousMonth) {
        days.add(
            CalendarDay(
                calendar.time,
                false,
                currentWeek,
                getNotesForDate(notes, calendar.time)
            )
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            currentWeek++
        }
    }

    // Reset to first day of current month
    calendar.time = currentDate.time
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    // Add days of current month
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..daysInMonth) {
        days.add(
            CalendarDay(
                calendar.time,
                true,
                currentWeek,
                getNotesForDate(notes, calendar.time)
            )
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            currentWeek++
        }
    }

    // Add days from next month to complete the grid (42 cells total for 6 weeks)
    val remainingDays = 42 - days.size
    for (i in 0 until remainingDays) {
        days.add(
            CalendarDay(
                calendar.time,
                false,
                currentWeek,
                getNotesForDate(notes, calendar.time)
            )
        )
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
            currentWeek++
        }
    }

    return days
}

// Вспомогательная функция для сравнения дат
fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
}

private fun getYear(calendar: Calendar): String {
    return calendar.get(Calendar.YEAR).toString()
}

private fun getDayNumber(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.DAY_OF_MONTH).toString()
}

// Исправленная функция для получения названия месяца в именительном падеже
private fun getMonthName(calendar: Calendar): Int {
    val month = calendar.get(Calendar.MONTH)
    return when (month) {
        Calendar.JANUARY -> R.string.january
        Calendar.FEBRUARY -> R.string.february
        Calendar.MARCH -> R.string.march
        Calendar.APRIL -> R.string.april
        Calendar.MAY -> R.string.may
        Calendar.JUNE -> R.string.june
        Calendar.JULY -> R.string.july
        Calendar.AUGUST -> R.string.august
        Calendar.SEPTEMBER -> R.string.september
        Calendar.OCTOBER -> R.string.october
        Calendar.NOVEMBER -> R.string.november
        Calendar.DECEMBER -> R.string.december
        else -> R.string.january
    }
}

private fun getNotesForDate(notes: List<Note>, date: Date): List<Note> {
    val targetCalendar = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val targetYear = targetCalendar.get(Calendar.YEAR)
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

    return notes.filter { note ->
        note.createdAt.let { createdAt ->
            val reminderCalendar = Calendar.getInstance().apply {
                timeInMillis = createdAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val reminderYear = reminderCalendar.get(Calendar.YEAR)
            val reminderMonth = reminderCalendar.get(Calendar.MONTH)
            val reminderDay = reminderCalendar.get(Calendar.DAY_OF_MONTH)

            targetYear == reminderYear && targetMonth == reminderMonth && targetDay == reminderDay
        }
    }
}