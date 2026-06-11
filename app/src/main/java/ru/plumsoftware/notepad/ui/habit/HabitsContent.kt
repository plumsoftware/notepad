package ru.plumsoftware.notepad.ui.habit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.habit.HabitEntry
import ru.plumsoftware.notepad.data.model.habit.HabitWithHistory
import ru.plumsoftware.notepad.ui.NoteViewModel
import ru.plumsoftware.notepad.ui.elements.IOSCalendarView
import ru.plumsoftware.notepad.ui.elements.habits.HabitCard
import ru.plumsoftware.notepad.ui.elements.isSameDay
import ru.plumsoftware.notepad.ui.notes.getFancyDateTitle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HabitsContent(
    viewModel: NoteViewModel,
    navController: NavController,
    showEmptyStateButton: Boolean = true
) {
    val habitsWithHistory by viewModel.habits.collectAsState()
    var showCreateHabitScreen by remember { mutableStateOf(false) }

    // Состояние календаря
    var selectedHabitDate by remember { mutableStateOf(Date()) }
    var isCalendarExpanded by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val backgroundColor = MaterialTheme.colorScheme.background

    // 🔥 Проверка: Является ли выбранная дата "Сегодняшним днём"?
    val isDateToday = remember(selectedHabitDate) {
        isSameDay(selectedHabitDate, Date())
    }

    if (showCreateHabitScreen) {
        // Мы используем Screen composable, но можно открыть как Dialog.
        // Для iOS стиля лучше открывать как "Page Sheet".
        // Но в текущей навигации проще открыть отдельный экран через navController
        // ПРИМЕЧАНИЕ: Я бы рекомендовал добавить Screen.AddHabit в NavHost MainActivity.
        // Но пока можно сделать временное решение через переменную тут,
        // но лучше вызвать navController.navigate(Screen.AddHabit.route)

        // Временная заглушка (если ты еще не добавил AddHabit в навигацию):
        /* AddHabitDialog(onDismiss = { showCreateHabitScreen = false }) */
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Заголовок по центру
//        Text(
//            stringResource(R.string.habits_title),
//            textAlign = TextAlign.Center,
//            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp)
//        )
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            IOSCalendarView(
                notes = emptyList(),
                habits = habitsWithHistory,
                selectedDate = selectedHabitDate,
                isMonthExpanded = isCalendarExpanded,
                onDateSelected = { date -> selectedHabitDate = date },
                onExpandChange = { isCalendarExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    // Можно убрать тень или фон, чтобы он сливался, или оставить как карточку
                    .background(backgroundColor)
            )

            if (habitsWithHistory.isEmpty()) {
                // --- ПУСТОЕ СОСТОЯНИЕ ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.TaskAlt,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.habit_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        if (showEmptyStateButton) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { navController.navigate("add_habit") },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(text = stringResource(R.string.habit_new))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ВИДЖЕТ ПРОГРЕССА
                    // (Показывает прогресс за выбранный день)
                    item {
                        // Считаем прогресс для ВЫБРАННОЙ даты
                        val total = habitsWithHistory.size
                        val done = habitsWithHistory.count {
                            checkIfCompletedForDate(
                                it.history,
                                selectedHabitDate
                            )
                        }
                        val progress = if (total > 0) done.toFloat() / total else 0f

                        IOSHabitProgressHeaderInternal(progress, done, total, selectedHabitDate)
                    }

                    item {
                        Text(
                            text = stringResource(R.string.habit_goals),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    items(habitsWithHistory, key = { it.habit.id }) { item ->
                        val habit = item.habit
                        var showHabitMenu by remember { mutableStateOf(false) }
                        val history = item.history

                        // 🔥 1. Проверяем статус для ВЫБРАННОЙ даты (чтобы видеть историю)
                        val isCompletedOnSelectedDate = remember(history, selectedHabitDate) {
                            checkIfCompletedForDate(history, selectedHabitDate)
                        }

                        val streak = remember(history) { calculateStreak(history) }
                        val weekHistory = remember(history) { buildWeekHistory(history) }

                        Box(modifier = Modifier.animateItem()) {
                            HabitCard(
                                title = habit.title,
                                emoji = habit.emoji,
                                streak = streak,
                                color = Color(habit.color.toULong()),
                                weekHistory = weekHistory,
                                isCompletedToday = isCompletedOnSelectedDate,
                                onToggle = {
                                    // 🔥 2. Блокируем изменение, если дата не сегодня
                                    if (isDateToday) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleHabit(habit.id)
                                    } else {
                                        // Опционально: Сообщить пользователю
                                        // Toast.makeText(context, "Нельзя менять прошлое", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onLongClick = { showHabitMenu = true },
                                onClick = { navController.navigate("edit_habit/${habit.id}") }
                            )

                            // Меню (без изменений)
                            DropdownMenu(
                                expanded = showHabitMenu,
                                onDismissRequest = { showHabitMenu = false },
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Изменить") },
                                    onClick = {
                                        showHabitMenu = false
                                        navController.navigate("edit_habit/${habit.id}")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.delete),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showHabitMenu = false
                                        viewModel.deleteHabit(habit)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }
}

// Хелпер: Виджет прогресса для реальных данных
@Composable
fun IOSHabitProgressWidget(
    habits: List<HabitWithHistory>,
    selectedDate: Date
) {
    // Считаем сколько выполнено СЕГОДНЯ
    val total = habits.size
    val done = habits.count { checkIfCompletedForDate(it.history, selectedDate) }
    val progress = if (total > 0) done.toFloat() / total else 0f

    // (Используем тот же код дизайна, что я давал ранее, но с реальными данными)
    // ... [См. код IOSHabitProgressHeader из предыдущих ответов, он идеально подходит] ...
    // Вставь сюда вызов того же UI кода
    IOSHabitProgressHeaderInternal(progress, done, total, selectedDate)
}

// Вспомогательная функция (UI виджета прогресса)
@Composable
fun IOSHabitProgressHeaderInternal(
    progress: Float,
    done: Int,
    total: Int,
    selectedHabitDate: Date
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800))
    val sectionColor = MaterialTheme.colorScheme.surface

    val currentDate = Calendar.getInstance().time

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = sectionColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        if (isSameDay(date1 = currentDate, date2 = selectedHabitDate)) stringResource(R.string.today) else getFancyDateTitle(selectedHabitDate),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (progress == 1f)
                            stringResource(R.string.all_done_title)
                        else
                            stringResource(R.string.progress_status, done, total),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// --- Logic Helpers ---

fun checkIfCompletedToday(history: List<HabitEntry>): Boolean {
    val today = getStartOfDay()
    return history.any { it.date == today }
}

fun calculateStreak(history: List<HabitEntry>): Int {
    // Простейшая логика стрика
    // В реале сложнее, но для старта: сколько дней подряд с сегодняшнего/вчерашнего
    val sortedDates = history.map { it.date }.sortedDescending().distinct()
    var streak = 0
    var checkDate = getStartOfDay()

    // Если сегодня не выполнено, проверяем, была ли выполнена вчера (чтобы стрик не обнулился раньше времени)
    if (!sortedDates.contains(checkDate)) {
        checkDate -= 24 * 60 * 60 * 1000 // Вчера
    }

    for (date in sortedDates) {
        if (date == checkDate) {
            streak++
            checkDate -= 24 * 60 * 60 * 1000 // Идем назад
        } else if (date < checkDate) {
            break // Дырка в днях
        }
    }
    return streak
}

fun checkIfCompletedForDate(history: List<HabitEntry>, date: Date): Boolean {
    val calendar = Calendar.getInstance().apply { time = date }
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val targetTime = calendar.timeInMillis

    return history.any { it.date == targetTime }
}

fun getStartOfDay(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun buildWeekHistory(history: List<HabitEntry>): List<Boolean> {
    val calendar = Calendar.getInstance()
    return (6 downTo 0).map { daysAgo ->
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        checkIfCompletedForDate(history, calendar.time)
    }
}