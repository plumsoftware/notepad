package ru.plumsoftware.notepad.ui.elements

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.window.Dialog
import ru.plumsoftware.notepad.R
import ru.plumsoftware.notepad.data.model.Note
import java.util.Calendar
import java.util.Date

/**
 * Кастомный календарь в стиле макета: выбор диапазона дат (от — до)
 * для фильтрации заметок на главном экране.
 */
@Composable
fun NoteCalendarRangeDialog(
    notes: List<Note>,
    initialRange: NoteDateRange?,
    onConfirm: (NoteDateRange?) -> Unit,
    onDismiss: () -> Unit
) {
    var start by remember { mutableStateOf(initialRange?.let { startOfDay(it.startMillis) }) }
    var end by remember { mutableStateOf(initialRange?.let { startOfDay(it.endMillis) }) }

    // Отображаемый месяц (по первой дате диапазона или текущий)
    var displayedMonth by remember {
        mutableStateOf(Calendar.getInstance().apply {
            timeInMillis = start ?: System.currentTimeMillis()
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    val days = remember(displayedMonth.timeInMillis) { buildMonthGrid(displayedMonth) }

    fun onDayTap(dayMillis: Long) {
        val s = start
        val e = end
        when {
            s == null || e != null -> {
                start = dayMillis; end = null
            }
            dayMillis < s -> {
                start = dayMillis; end = s
            }
            else -> {
                end = dayMillis
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // --- Шапка: месяц + стрелки ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = getMonthAndYearString(displayedMonth.time)
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalendarNavButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft) {
                        displayedMonth = (displayedMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }
                    CalendarNavButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight) {
                        displayedMonth = (displayedMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Дни недели ---
            val weekDays = listOf(
                stringResource(R.string.monday) to false,
                stringResource(R.string.tuesday) to false,
                stringResource(R.string.wednesday) to false,
                stringResource(R.string.thursday) to false,
                stringResource(R.string.friday) to false,
                stringResource(R.string.saturday) to true,
                stringResource(R.string.sunday) to true
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { (label, isWeekend) ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isWeekend) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Сетка дней (6 недель) ---
            val displayedMonthIndex = displayedMonth.get(Calendar.MONTH)
            days.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { dayMillis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                        val isCurrentMonth = cal.get(Calendar.MONTH) == displayedMonthIndex
                        val dow = cal.get(Calendar.DAY_OF_WEEK)
                        val isWeekend = dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
                        val isStart = start != null && dayMillis == start
                        val isEnd = end != null && dayMillis == end
                        val inRange = start != null && end != null &&
                                dayMillis in start!!..end!!
                        val hasNotes = getNotesForDate(notes, Date(dayMillis)).isNotEmpty()

                        NoteCalendarDayCell(
                            day = cal.get(Calendar.DAY_OF_MONTH),
                            isCurrentMonth = isCurrentMonth,
                            isWeekend = isWeekend,
                            isEndpoint = isStart || isEnd,
                            inRange = inRange,
                            hasNotes = hasNotes,
                            onClick = { onDayTap(dayMillis) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Кнопки ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            start = null; end = null
                            onConfirm(null)
                            onDismiss()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .blueShadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = start != null) {
                            val s = start
                            if (s != null) {
                                onConfirm(NoteDateRange(s, end ?: s))
                            }
                            onDismiss()
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.done),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun NoteCalendarDayCell(
    day: Int,
    isCurrentMonth: Boolean,
    isWeekend: Boolean,
    isEndpoint: Boolean,
    inRange: Boolean,
    hasNotes: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellShape = RoundedCornerShape(12.dp)
    val bg = when {
        isEndpoint -> MaterialTheme.colorScheme.primary
        inRange -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isEndpoint -> MaterialTheme.colorScheme.onPrimary
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
        isWeekend -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(cellShape)
            .background(bg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isEndpoint) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (hasNotes && !isEndpoint) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
            )
        }
    }
}

/** Строит сетку из 42 дней (6 недель), начиная с понедельника, для месяца [month]. */
private fun buildMonthGrid(month: Calendar): List<Long> {
    val calendar = (month.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val offset = when (firstDayOfWeek) {
        Calendar.MONDAY -> 0
        Calendar.SUNDAY -> 6
        else -> firstDayOfWeek - Calendar.MONDAY
    }
    calendar.add(Calendar.DAY_OF_MONTH, -offset)

    val result = mutableListOf<Long>()
    repeat(42) {
        result.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return result
}
