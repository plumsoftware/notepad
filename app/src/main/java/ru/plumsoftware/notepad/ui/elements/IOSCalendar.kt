package ru.plumsoftware.notepad.ui.elements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Note
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory
import ru.plumsoftware.notepad.ui.notes.calculateDailyHabitProgress
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.text.chunked
import kotlin.text.forEach

// Обновляем data class для хранения информации о неделе
data class CalendarDay(
    val date: Date,
    val isCurrentMonth: Boolean,
    val week: Int, // Добавляем номер недели
    val notes: List<Note> = emptyList(),
    val habitProgress: Float = 0f
)

@Composable
fun IOSCalendarView(
    notes: List<Note>,
    habits: List<HabitWithHistory>,
    selectedDate: Date,
    isMonthExpanded: Boolean,
    onDateSelected: (Date) -> Unit,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Внутренняя дата для навигации (чтобы можно было листать месяцы, не меняя выбранный день)
    var displayedDate by remember { mutableStateOf(selectedDate) }

    // Передаем habits в генератор
    val calendarDays = remember(displayedDate, notes, habits) {
        generateCalendarDays(Calendar.getInstance().apply { time = displayedDate }, notes, habits)
    }

    Column(modifier = modifier) {
        // --- 1. ШАПКА КАЛЕНДАРЯ (Месяц + Стрелки) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Название месяца (с переключателем)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onExpandChange(!isMonthExpanded) }
                    .padding(4.dp)
            ) {
                // "Январь 2025" (с заглавной буквы)
                Text(
                    text = getMonthAndYearString(displayedDate).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                // Стрелочка раскрытия
                Icon(
                    imageVector = if(isMonthExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Стрелки Влево/Вправо
            Row {
                IconButton(onClick = {
                    displayedDate = addTime(displayedDate, isMonthExpanded, -1)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.calendar_previous_month),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    displayedDate = addTime(displayedDate, isMonthExpanded, 1)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.calendar_next_month),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // --- 2. ДНИ НЕДЕЛИ ---
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            val weekDays = listOf(
                stringResource(R.string.monday),
                stringResource(R.string.tuesday),
                stringResource(R.string.wednesday),
                stringResource(R.string.thursday),
                stringResource(R.string.friday),
                stringResource(R.string.saturday),
                stringResource(R.string.sunday)
            )
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        // --- 3. СЕТКА (МЕСЯЦ / НЕДЕЛЯ) ---
        AnimatedContent(
            targetState = isMonthExpanded,
            transitionSpec = {
                // Анимация разворачивания/сворачивания
                fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
            },
            label = "CalendarAnimation"
        ) { isExpanded ->
            if (isExpanded) {
                // === МЕСЯЦ ===
                Column {
                    val weeks = calendarDays.chunked(7)
                    weeks.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                IOSDayCell(
                                    day = day,
                                    isSelected = isSameDay(day.date, selectedDate),
                                    onDayClick = {
                                        onDateSelected(day.date)
                                        displayedDate = day.date
                                    },
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                // === НЕДЕЛЯ ===
                // Находим неделю, где находится displayedDate
                val currentWeek = findWeekForDate(calendarDays, displayedDate)

                Row(modifier = Modifier.fillMaxWidth()) {
                    currentWeek.forEach { day ->
                        IOSDayCell(
                            day = day,
                            isSelected = isSameDay(day.date, selectedDate),
                            onDayClick = {
                                onDateSelected(day.date)
                                displayedDate = day.date
                            },
                            modifier = Modifier.weight(1f).aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

// Компонент одного дня
@Composable
fun IOSDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onDayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = isSameDay(day.date, Date())

    // Цвета
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = when {
        isSelected -> Color.White
        isToday -> MaterialTheme.colorScheme.primary
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal

    // Цвета для кольца прогресса
    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant // Серый трек

    Box(
        modifier = modifier
            .padding(2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDayClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Круг с датой
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getDayNumber(day.date),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = fontWeight,
                        fontSize = 15.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // 🔥 ИНДИКАТОРЫ 🔥

            // Если есть прогресс привычек -> Рисуем кольцо (или точку, если 100%)
            // Если есть только заметки -> Рисуем точку

            if (day.habitProgress > 0) {
                // Рисуем маленькое кольцо прогресса
                Canvas(modifier = Modifier.size(6.dp)) {
                    // Трек (серый круг)
                    drawCircle(
                        color = trackColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                    )
                    // Прогресс (цветная дуга)
                    drawArc(
                        color = if(isSelected) Color.White else ringColor, // Белый, если фон синий
                        startAngle = -90f,
                        sweepAngle = 360 * day.habitProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                }
            } else if (day.notes.isNotEmpty()) {
                // Если привычек нет, но есть заметки - классическая точка
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                // Пустышка, чтобы высота ячейки не скакала
                Spacer(modifier = Modifier.size(6.dp)) // Высота как у Canvas
            }
        }
    }
}

// Хелперы для логики переключения дат
fun addTime(current: Date, isMonth: Boolean, amount: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = current
    if (isMonth) {
        cal.add(Calendar.MONTH, amount)
    } else {
        cal.add(Calendar.WEEK_OF_YEAR, amount)
    }
    return cal.time
}

fun findWeekForDate(allDays: List<CalendarDay>, targetDate: Date): List<CalendarDay> {
    // Поскольку allDays - это плоский список на месяц (и немного вокруг),
    // нам нужно найти индекс дня и взять кусок недели.
    // Сначала проверим, входит ли целевая дата в сгенерированный месяц.
    // Если мы перелистнули неделю далеко, allDays может быть не тот.
    // Но так как displayedDate драйвит генерацию days, они должны совпадать.

    val calTarget = Calendar.getInstance().apply { time = targetDate }
    val targetDayYear = calTarget.get(Calendar.DAY_OF_YEAR)

    // Ищем день в списке
    val index = allDays.indexOfFirst {
        val calIt = Calendar.getInstance().apply { time = it.date }
        calIt.get(Calendar.DAY_OF_YEAR) == targetDayYear
    }

    if (index == -1) return emptyList() // Fallback

    // Вычисляем начало недели.
    // Предполагаем, что allDays начинается с ПН (так работает твой generateCalendarDays)
    val weekStartIndex = (index / 7) * 7
    // Берем 7 дней
    return allDays.subList(weekStartIndex, minOf(weekStartIndex + 7, allDays.size))
}

// Формат заголовка списка: "Понедельник, 12 Января" (Зависит от локали телефона)
fun getFancyDateTitle(date: Date): String {
    val fmt = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    return fmt.format(date).replaceFirstChar { it.uppercase() }
}

// Формат заголовка календаря: "Январь 2025" (Зависит от локали телефона)
fun getMonthAndYearString(date: Date): String {
    // LLLL - полное название месяца (Январь), yyyy - год
    return SimpleDateFormat("LLLL yyyy", Locale.getDefault()).format(date)
}

// Обновляем функцию генерации дней для включения номера недели
private fun generateCalendarDays(
    currentDate: Calendar,
    notes: List<Note>,
    habits: List<HabitWithHistory>
): List<CalendarDay> {
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
    for (i in 0 until 42) {
        val date = calendar.time

        // Определяем, относится ли день к текущему месяцу (для цвета текста)
        // (Логика isCurrentMonth может быть чуть сложнее, если используем сквозной скролл,
        // но для статической сетки берем сравнение месяца)
        val isCurrentMonth = calendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)

        days.add(
            CalendarDay(
                date = date,
                isCurrentMonth = isCurrentMonth,
                week = currentWeek,
                notes = getNotesForDate(notes, date),
                habitProgress = calculateDailyHabitProgress(date, habits) // <-- СЧИТАЕМ ПРОГРЕСС
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

fun getDayNumber(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar.get(Calendar.DAY_OF_MONTH).toString()
}

fun getNotesForDate(notes: List<Note>, date: Date): List<Note> {
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