package ru.plumsoftware.notepad.ui.elements.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmojiPickerDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val emojis = listOf(
        "💧", "🏃", "🏋️", "🧘", "📚", "💤",
        "💰", "💊", "🥗", "📵", "🧹", "🎸",
        "🐶", "🎨", "💻", "🥬", "🚬", "❤️"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Выберите иконку",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            // LazyVerticalGrid не всегда хорошо работает внутри AlertDialog из-за высоты.
            // Используем фиксированную высоту или Column с Row.
            // Но Grid удобнее:
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) // Ограничиваем высоту
            ) {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis.size) { index ->
                        val emoji = emojis[index]
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 32.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp) // iOS style radius
    )
}