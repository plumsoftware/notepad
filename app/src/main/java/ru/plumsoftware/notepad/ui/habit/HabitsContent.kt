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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import ru.plumsoftware.notepad.ui.elements.HabitCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitsContent(
    viewModel: NoteViewModel,
    navController: NavController
) {
    // Подписываемся на список привычек
    val habitsWithHistory by viewModel.habits.collectAsState()

    // Стейт для диалога создания
    var showCreateHabitScreen by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current

    // Если открыт экран создания, показываем его поверх
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ЗАГОЛОВОК (IOS Large Title)
            // Мы не используем стандартный TopAppBar здесь, чтобы был "прозрачный" стиль
            Text(
                text = stringResource(R.string.habits_title), // "Привычки"
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
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
                            imageVector = Icons.Rounded.TaskAlt, // Или красивая картинка
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
                        Spacer(modifier = Modifier.height(24.dp))
                        // Кнопка "Создать первую"
                        Button(
                            onClick = { navController.navigate("add_habit") }, // Добавь route
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = stringResource(R.string.habit_new))
                        }
                    }
                }
            } else {
                // --- СПИСОК ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. ВИДЖЕТ ПРОГРЕССА
                    item {
                        // Подсчет статистики "на лету"
                        IOSHabitProgressWidget(habits = habitsWithHistory)
                    }

                    item {
                        Text(
                            text = stringResource(R.string.habit_goals),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    // 2. КАРТОЧКИ
                    items(habitsWithHistory, key = { it.habit.id }) { item ->
                        val habit = item.habit

                        // --- CONTEXT MENU STATE ---
                        var showHabitMenu by remember { mutableStateOf(false) }

                        val history = item.history
                        val isCompletedToday = remember(history) { checkIfCompletedToday(history) }
                        val streak = remember(history) { calculateStreak(history) }

                        Box(modifier = Modifier.animateItem()) {
                            HabitCard(
                                title = habit.title,
                                emoji = habit.emoji, // <-- Новый параметр
                                streak = streak,
                                color = Color(habit.color.toULong()),
                                isCompletedToday = isCompletedToday,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleHabit(habit.id)
                                },
                                onLongClick = {
                                    showHabitMenu = true // Открываем меню
                                },
                                onClick = {
                                    // ОТКРЫВАЕМ РЕДАКТИРОВАНИЕ
                                    // Используем маршрут add_habit и передаем аргумент habitId
                                    navController.navigate("edit_habit/${habit.id}")
                                }
                            )

                            // --- MENU ---
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
                                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        showHabitMenu = false
                                        viewModel.deleteHabit(habit)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }

                    // Отступ под бар
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // FAB для добавления (если список не пустой)
        if (habitsWithHistory.isNotEmpty()) {
            FloatingActionButton(
                onClick = { navController.navigate("add_habit") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 20.dp) // Поднимаем над BottomBar
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// Хелпер: Виджет прогресса для реальных данных
@Composable
fun IOSHabitProgressWidget(habits: List<HabitWithHistory>) {
    // Считаем сколько выполнено СЕГОДНЯ
    val total = habits.size
    val done = habits.count { checkIfCompletedToday(it.history) }
    val progress = if (total > 0) done.toFloat() / total else 0f

    // (Используем тот же код дизайна, что я давал ранее, но с реальными данными)
    // ... [См. код IOSHabitProgressHeader из предыдущих ответов, он идеально подходит] ...
    // Вставь сюда вызов того же UI кода
    IOSHabitProgressHeaderInternal(progress, done, total)
}

// Вспомогательная функция (UI виджета прогресса)
@Composable
fun IOSHabitProgressHeaderInternal(progress: Float, done: Int, total: Int) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(0.05f)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        stringResource(R.string.today),
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

fun getStartOfDay(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}