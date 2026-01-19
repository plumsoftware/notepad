package ru.plumsoftware.notepad.ui.elements.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WeekDaySelector(
    selectedDays: Set<Int>, // Набор дней (например: 2, 3, 4...)
    onDayToggle: (Int) -> Unit,
    activeColor: Color
) {
    // Пары: (Calendar.CONSTANT, "Буква")
    // Порядок: Пн(2), Вт(3), Ср(4), Чт(5), Пт(6), Сб(7), Вс(1)
    val weekDays = listOf(
        Pair(java.util.Calendar.MONDAY, "Пн"),
        Pair(java.util.Calendar.TUESDAY, "Вт"),
        Pair(java.util.Calendar.WEDNESDAY, "Ср"),
        Pair(java.util.Calendar.THURSDAY, "Чт"),
        Pair(java.util.Calendar.FRIDAY, "Пт"),
        Pair(java.util.Calendar.SATURDAY, "Сб"),
        Pair(java.util.Calendar.SUNDAY, "Вс")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { (calendarId, label) ->
            val isSelected = selectedDays.contains(calendarId)

            // Анимация цвета
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) activeColor else Color.LightGray.copy(alpha = 0.2f),
                label = "dayBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Gray,
                label = "dayText"
            )

            Box(
                modifier = Modifier
                    .size(40.dp) // Размер кружка
                    .clip(CircleShape)
                    .background(bgColor)
                    .clickable { onDayToggle(calendarId) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.first().toString(), // Берем первую букву
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}