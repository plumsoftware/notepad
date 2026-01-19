package ru.plumsoftware.notepad.ui.elements.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorSelectorRow(
    selectedColor: Color,
    onColorSelect: (Color) -> Unit
) {
    val colors = listOf(
        Color(0xFF007AFF), // Blue
        Color(0xFF34C759), // Green
        Color(0xFFFF9500), // Orange
        Color(0xFFFF2D55), // Pink/Red
        Color(0xFF5856D6), // Purple
        Color(0xFF5AC8FA), // Light Blue
        Color(0xFFFFCC00), // Yellow
        Color(0xFFA2845E)  // Brown
    )

    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    // Индикатор выбора (Белая точка с тенью для контраста)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.Black.copy(alpha = 0.1f), CircleShape)
                    )
                }
            }
        }
    }
}